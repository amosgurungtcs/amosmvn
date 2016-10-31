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
package com.day.cq.wcm.foundation.forms.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.foundation.forms.FormsConstants;
import com.day.cq.wcm.foundation.forms.FormsManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link FormsManager}.
 */
public class FormsManagerImpl implements FormsManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ResourceResolver resolver;

    /** Search paths for constraints and actions.*/
    private String[] searchPaths;

    protected FormsManagerImpl(ResourceResolver resolver) {
        this.resolver = resolver;

        // initialize search paths
        this.searchPaths = resolver.getSearchPath();
        for (int i = 0; i < searchPaths.length; i++) {
            this.searchPaths[i] = this.searchPaths[i].substring(0, this.searchPaths[i].length() - 1);
        }
    }

    /**
     * @see com.day.cq.wcm.foundation.forms.FormsManager#getActions()
     */
    public Iterator<ComponentDescription> getActions() {
        return this.search(FormsConstants.RT_FORM_ACTION).iterator();
    }

    /**
     * @see com.day.cq.wcm.foundation.forms.FormsManager#getConstraints()
     */
    public Iterator<ComponentDescription> getConstraints() {
        return this.search(FormsConstants.RT_FORM_CONSTRAINT).iterator();
    }

    /**
     * @see com.day.cq.wcm.foundation.forms.FormsManager#getDialogPathForAction(String)
     */
    public String getDialogPathForAction(String resourceType) {
        if (resourceType == null) {
            return null;
        }
        for (final String path : searchPaths) {
            final String dialogPath = path + '/' + resourceType + "/dialog";
            if (resolver.getResource(dialogPath) != null ) {
                return dialogPath;
            }
        }
        return null;
    }

    private Collection<ComponentDescription> search(String type) {
        final Map<String, ComponentDescription> map = new HashMap<String, ComponentDescription>();
        final List<String> disabledComponents = new ArrayList<String>();
        for(final String path : this.searchPaths) {
            final StringBuilder buffer = new StringBuilder("/jcr:root");
            buffer.append(path);
            buffer.append("//* [@");
            buffer.append(FormsConstants.PROPERTY_RT);
            buffer.append("='");
            buffer.append(type);
            buffer.append("']");

            logger.debug("Query: {}", buffer.toString());
            final Iterator<Resource> i = resolver.findResources(buffer.toString(), "xpath");
            while ( i.hasNext() ) {
                final Resource rsrc = i.next();
                // check if disabled
                final ValueMap properties = ResourceUtil.getValueMap(rsrc);
                // get resource type
                final String rt = rsrc.getPath().substring(path.length() + 1);
                if ( properties.get(FormsConstants.COMPONENT_PROPERTY_ENABLED, Boolean.TRUE) ) {
                    if ( ! map.containsKey(rt) && !disabledComponents.contains(rt)) {
                        map.put(rt, new ComponentDescriptionImpl(rt, ResourceUtil.getName(rsrc), properties));
                    }
                } else {
                    disabledComponents.add(rt);
                }
            }
        }
        // now sort the entries
        final List<ComponentDescription> entries = new ArrayList<ComponentDescription>(map.values());
        Collections.sort(entries);
        return entries;
    }

    public final static class ComponentDescriptionImpl implements ComponentDescription, Comparable<ComponentDescription> {

        private final String resourceType;
        private final String title;
        private final String hint;
        private final int    order;

        public ComponentDescriptionImpl(final String rt, final String defaultName, final ValueMap props) {
            this.resourceType = rt;
            this.title = props.get(JcrConstants.JCR_TITLE, defaultName);
            this.order = props.get(FormsConstants.COMPONENT_PROPERTY_ORDER, 0);
            this.hint = props.get(FormsConstants.COMPONENT_PROPERTY_HINT, String.class);
        }

        /**
         * @see com.day.cq.wcm.foundation.forms.FormsManager.ComponentDescription#getResourceType()
         */
        public String getResourceType() {
            return this.resourceType;
        }

        /**
         * @see com.day.cq.wcm.foundation.forms.FormsManager.ComponentDescription#getTitle()
         */
        public String getTitle() {
            return this.title;
        }

        public int getOrder() {
            return this.order;
        }

        /**
         * @see com.day.cq.wcm.foundation.forms.FormsManager.ComponentDescription#getHint()
         */
        public String getHint() {
            return this.hint;
        }

        /**
         * @see Comparable#compareTo(Object)
         */
        public int compareTo(ComponentDescription o) {
            final ComponentDescriptionImpl obj = (ComponentDescriptionImpl)o;
            if ( this.order < obj.order ) {
                return -1;
            } else if ( this.order == obj.order ) {
                return this.title.compareTo(obj.title);
            }
            return 1;
        }

    }

}
