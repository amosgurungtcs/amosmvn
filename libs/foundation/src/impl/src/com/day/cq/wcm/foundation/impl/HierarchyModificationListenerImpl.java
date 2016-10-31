/*
 * Copyright 1997-2010 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
package com.day.cq.wcm.foundation.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.observation.JackrabbitEventFilter;
import org.apache.jackrabbit.api.observation.JackrabbitObservationManager;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.foundation.HierarchyModificationListener;
import com.day.text.Text;

/**
 * <code>HierarchyModificationListenerImpl</code> implements a very simple
 * modification model:
 * - modifications are aggregated on the 3rd level (i.e. /content/geometrixx/en)
 * - only property changes on a jcr:content are registered
 * - only modifications on the 3 - 6 level are registered
 */
@Component(metatype = false, immediate = true)
@Service(HierarchyModificationListener.class)
public class HierarchyModificationListenerImpl implements HierarchyModificationListener, EventListener {

    @Property(value = {"/content"})
    private final static String PROPERTY_PATHS = "paths";

    @Property(value = {"/content/dam"} )
    private final static String PROPERTY_EXCLUDED_PATHS = "excludedPaths";

    private final static String[] DEFAULT_PATHS = new String[]{"/content"};

    private final static String HIERARCHYMOD_LISTENER_SERVICE="hierarchymodification-listener-service";

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(HierarchyModificationListenerImpl.class);

    private static final Set<String> IGNORED_PROPERTIES = new HashSet<String>();
    static {
        IGNORED_PROPERTIES.add(NameConstants.PN_LAST_MOD);
        IGNORED_PROPERTIES.add(NameConstants.PN_LAST_MOD_BY);
        IGNORED_PROPERTIES.add(NameConstants.PN_PAGE_LAST_MOD);
        IGNORED_PROPERTIES.add(NameConstants.PN_PAGE_LAST_MOD_BY);
        IGNORED_PROPERTIES.add(NameConstants.PN_PAGE_LAST_REPLICATED);
        IGNORED_PROPERTIES.add(NameConstants.PN_PAGE_LAST_REPLICATED_BY);
        IGNORED_PROPERTIES.add(NameConstants.PN_PAGE_LAST_REPLICATION_ACTION);
    }

    protected Session session;

    private long startTime;

    @Reference
    private SlingRepository repository;

    private final Map<String, Long> modifications = new HashMap<String, Long>();

    protected void activate(org.osgi.service.component.ComponentContext context)
            throws RepositoryException {
        try {
            session = repository.loginService(HIERARCHYMOD_LISTENER_SERVICE, null);
            startTime = System.currentTimeMillis();

            synchronized (modifications) {
                modifications.clear();
            }

            String[] configuredPaths = null;
            String[] configuredExcludedPaths = null;
            if (context != null && context.getProperties() != null) {
                configuredPaths = PropertiesUtil.toStringArray(context.getProperties().get(PROPERTY_PATHS));
                configuredExcludedPaths = PropertiesUtil.toStringArray(context.getProperties().get(PROPERTY_EXCLUDED_PATHS));
            }

            if (configuredPaths == null) {
                configuredPaths = DEFAULT_PATHS;
            }
            
            JackrabbitEventFilter eventFilter = new JackrabbitEventFilter()
                    .setAbsPath(configuredPaths[0])
                    .setEventTypes(Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED | Event.PROPERTY_ADDED | Event.PROPERTY_REMOVED)
                    .setIsDeep(true)
                    .setNoLocal(true);

            if (configuredPaths.length > 1) {
                eventFilter.setAdditionalPaths(configuredPaths);
            }

            if (configuredExcludedPaths != null && configuredExcludedPaths.length > 0) {
                eventFilter.setExcludedPaths(configuredExcludedPaths);
            }

            JackrabbitObservationManager observationManager = (JackrabbitObservationManager) session.getWorkspace().getObservationManager();
            observationManager.addEventListener(this, eventFilter);

            log.info("Hierarchy Modification Listener started.");
        } catch (LoginException e) {
            log.error("Unable to obtain a service session for adding hierarchy modification listener",e);
        }
    }

    /**
     * Deactivates the LinkChecker service.
     *
     * @param componentContext The component context
     */
    protected void deactivate(org.osgi.service.component.ComponentContext componentContext) {
        if (session != null) {
            try {
                session.getWorkspace().getObservationManager().removeEventListener(this);
            } catch (RepositoryException e) {
                // ignore
            }
            session.logout();
            session = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public long getLastModified(String path) {
        Long lm;
        synchronized (modifications) {
            lm = modifications.get(path);
        }
        return lm == null ? startTime : lm;
    }

    /**
     * {@inheritDoc}
     */
    public void onEvent(EventIterator events) {
        Set<String> paths = new HashSet<String>();
        try {
            while (events.hasNext()) {
                Event e = events.nextEvent();
                StringBuilder path = new StringBuilder();
                String aggregatePath = null;
                String[] names = Text.explode(e.getPath(), '/');
                int pageLevel = 0;
                while (pageLevel < names.length
                        && !names[pageLevel].equals("jcr:content")) {
                    path.append("/").append(names[pageLevel]);
                    pageLevel++;
                    if (pageLevel == 3) {
                        // todo: make configurable
                        aggregatePath = path.toString();
                    }
                }
                int relLevel = names.length - pageLevel;
                if (log.isDebugEnabled()) {
                    log.debug("Modification on {}. page={}, pageLevel={}, relLevel={}", new Object[]{
                            e.getPath(), path, pageLevel, relLevel}
                    );
                }
                // only consider modifications on level 3 - 6
                // todo: make configurable
                if (aggregatePath == null || pageLevel > 6) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rejecting {} cause out of scope. level {} not within [3,6]", path, pageLevel);
                    }
                    continue;
                }
                // reject property modifications not on jcr:content

                switch (e.getType()) {
                    case Event.NODE_ADDED:
                    case Event.NODE_MOVED:
                    case Event.NODE_REMOVED:
                        // reject node modification within page
                        if (relLevel > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Rejecting {} cause node modification in jcr:content", path);
                            }
                            continue;
                        }
                        break;
                    case Event.PROPERTY_ADDED:
                    case Event.PROPERTY_CHANGED:
                    case Event.PROPERTY_REMOVED:
                        // reject property modifications not on jcr:content
                        if (relLevel > 2) {
                            if (log.isDebugEnabled()) {
                                log.debug("Rejecting {} cause property modification not on jcr:content", path);
                            }
                            continue;
                        }
                        // reject some properties
                        if (IGNORED_PROPERTIES.contains(names[names.length-1])) {
                            if (log.isDebugEnabled()) {
                                log.debug("Rejecting {} cause property is ignored", path);
                            }
                            continue;
                        }
                }
                // register path
                paths.add(aggregatePath);
            }
        } catch (RepositoryException e) {
            log.warn("Error while reading event info", e);
        }
        // touch paths
        Long lm = System.currentTimeMillis();
        synchronized (modifications) {
            for (String path: paths) {
                log.debug("Touching {} at {}", path, lm);
                modifications.put(path, lm);
            }
        }
    }

}