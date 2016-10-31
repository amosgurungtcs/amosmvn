/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.day.cq.wcm.foundation.forms.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.text.Text;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.foundation.forms.FormsConstants;
import com.day.cq.wcm.foundation.forms.FormsHelper;
import com.day.cq.wcm.foundation.forms.FormStoreService;

@Component(metatype = false, name = "com.day.cq.wcm.foundation.forms.FormStoreService",
        description = "Service for storing user generated content")
@Service
@Property(name = "service.description", value = "Foundation Form Actions Service")
public class FormStoreServiceImpl implements FormStoreService {

    private final Logger log = LoggerFactory.getLogger(FormStoreServiceImpl.class);

    private final static String FORM_STORE_SERVICE = "form-store-service";

    private final static String STORE_CONTENT_PRIVILEGE = "cq:storeUGC";

    @Reference
    SlingRepository repository;

    @Reference(target="(sling.post.operation=modify)")
    private PostOperation modifyOperation;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference(target="(name=StartWorkflowPostProcessor)")
    private SlingPostProcessor startWorkflowPostProcessor;

    @Override
    public boolean runFormStoreAction(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        boolean propertiesStored;
        Resource resource = request.getResource();
        final ValueMap props = resource.getValueMap();

        // get the normalized absolute path below which the data will be stored
        String absPath = getStorePath(request);

        // perform the POST to the resource at the given path
        propertiesStored = storeUGC(request, absPath);

        // Set "formPath" and "sling:resourceType" on the parent resource
        // to allow bulk editor and forms payload summary respectively.

        String absParentPath = Text.getRelativeParent(absPath, 1);
        Map<String, Object> parentProperties = new HashMap<String, Object>();
        parentProperties.put("formPath", request.getParameter(FormsConstants.REQUEST_PROPERTY_FORM_START));
        parentProperties.put("sling:resourceType", "foundation/components/form/actions/showbulkeditor");
        propertiesStored = propertiesStored && storeProperties(request, absParentPath, parentProperties);

        request.setAttribute(FormsConstants.REQUEST_ATTR_WORKFLOW_PAYLOAD_PATH, absPath);

        // If a workflow model was specified, store it in
        // a request attribute for the StartWorkflowPostProcessor
        final String model = props.get(FormsConstants.START_PROPERTY_WORKFLOW_MODEL, null);
        if (model != null) {
            request.setAttribute(FormsConstants.REQUEST_ATTR_WORKFLOW_PATH, model);
        }

        // redirect

        String redirect = props.get("redirect", "");
        try {
            if (StringUtils.isEmpty(redirect) || !propertiesStored) {
                FormsHelper.redirectToReferrer(request, response, new HashMap<String, String[]>());
            } else {
                if (!redirect.contains(".")) {
                    redirect += ".html";
                }
                response.sendRedirect(request.getResourceResolver().map(request, redirect));
            }
        } catch (IOException e) {
            log.error("Error redirecting to {}", redirect);
        }

        // start the workflow

        try {
            startWorkflowPostProcessor.process(request, null);
        } catch (Exception e) {
            log.error("Error running the workflow {} on payload {}", model, absPath);
        }

        return propertiesStored;
    }

    /*
     * Returns a normalized absolute path below which the data will be stored
     */
    private String getStorePath(SlingHttpServletRequest request) {
        Resource resource = request.getResource();
        ResourceResolver resolver = request.getResourceResolver();
        final ValueMap props = resource.getValueMap();

        // Read the path from the resource properties
        String path = props.get(FormsConstants.START_PROPERTY_ACTION_PATH, "");
        if ((path != null && path.endsWith("/"))) {
            path = path + "*";
        }
        path = ResourceUtil.normalize(path);

        // If no path is specified, create a default path:
        // if the form is at:         /a/b/c/d
        // then the default path is:  /a/content/usergenerated/b/c/d/*
        // Example: input             /content/geometrixx/en/formpage
        //          output            /content/usergenerated/content/geometrixx/en/formpage/*
        if (StringUtils.isEmpty(path)) {
            PageManager pageManager = resolver.adaptTo(PageManager.class);
            Page currentPage = pageManager.getContainingPage(resource);
            if (currentPage == null) {
                return null;
            }
            final String pagePath = currentPage.getPath();
            String firstPart = Text.getAbsoluteParent(pagePath, 0);
            String secondPart = pagePath.substring(firstPart.length());
            path = firstPart + "/usergenerated/content" + secondPart + "/*";
        }

        // If path ends with / or /*, compute a unique name for the node to create,
        // so that we can pass it to StartWorkflowPostProcessor.
        if (StringUtils.isNotEmpty(path) && (path.endsWith("/") || path.endsWith("/*"))) {
            AtomicInteger uniqueIdCounter = new AtomicInteger();
            final String uniqueId = System.currentTimeMillis() + "_" + uniqueIdCounter.addAndGet(1);
            path = Text.getRelativeParent(path, 1) + "/" + uniqueId;
        }
        return path;
    }

    /*
     * Stores the request data to the resource defined by the path. It creates the resource if it does not exist.
     * It works as follows:
     * 1. it uses the request session
     * 2. if it failed and it the session has the cq:storeUGC privilege, it uses the service session
     */
    private boolean storeUGC(SlingHttpServletRequest request, String path) {
        return store(request, path, null);
    }

    /*
     * Stores the properties to the resource defined by the path. It creates the resource if it does not exist.
     * It works as follows:
     * 1. it uses the request session
     * 2. if it failed and it the session has the cq:storeUGC privilege, it uses the service session
     */
    private boolean storeProperties(SlingHttpServletRequest request, String path, Map<String, Object> properties) {
        return store(request, path, properties);
    }

    /*
     * If 'properties' is null, it stores the request data to the resource defined by the path.
     * Otherwise, it stores the properties to the resource defined by the path.
     * It creates the resource if it does not exist.
     * It works as follows:
     * 1. it uses the request session
     * 2. if it failed and it the session has the cq:storeUGC privilege, it uses the service session
     */
    private boolean store(SlingHttpServletRequest request, String path, Map<String, Object> properties) {
        boolean stored;
        ResourceResolver resolver = request.getResourceResolver();

        // store the data with the request session

        stored = doStore(request, resolver, path, properties);

        // if the data was not stored, check the privilege and try storing with the service user

        if (!stored) {
            try {
                final Session userSession = resolver.adaptTo(Session.class);
                String existingAncestorPath = findExistingPath(path, userSession);
                final AccessControlManager acMgr = userSession.getAccessControlManager();
                final Privilege[] storeContentPrivileges = new Privilege[] {acMgr.privilegeFromName(STORE_CONTENT_PRIVILEGE)};

                if (existingAncestorPath == null) {
                    log.error("User '{}' cannot read the node or its ancestors at {}", userSession.getUserID(), path);
                    return false;
                }

                // check if the user session can add UGC below the path

                if (acMgr.hasPrivileges(existingAncestorPath, storeContentPrivileges)) {
                    final Map<String, Object> authenticationInfo = new HashMap<String, Object>();
                    authenticationInfo.put(ResourceResolverFactory.SUBSERVICE, FORM_STORE_SERVICE);
                    ResourceResolver serviceResolver = null;
                    try {

                        // use a dedicated service session to store the data

                        serviceResolver = resolverFactory.getServiceResourceResolver(authenticationInfo);
                        stored = doStore(request, serviceResolver, path, properties);
                    } catch (LoginException e) {
                        log.error("Cannot store properties with the service user:", e);
                    } finally {
                        if (serviceResolver != null) {
                            serviceResolver.close();
                        }
                    }
                } else {
                    log.error("User '{}' does not have the cq:storeUGC privilege to add content below {}",
                            userSession.getUserID(), existingAncestorPath);
                }
            } catch (RepositoryException e) {
                log.error("Error storing the UGC", e);
            }
        }
        return stored;
    }

    /*
     * If 'properties' is null, it stores the request data to the resource defined by the path.
     * Otherwise, it stores the properties to the resource defined by the path.
     * It creates the resource if it does not exist.
     * It uses the given resource resolver to store the data.
     */
    private boolean doStore(SlingHttpServletRequest request, ResourceResolver resolver,
                                    String path, Map<String, Object> properties) {
        try {

            // create the store resource at the given path

            Resource storedResource = ResourceUtil.getOrCreateResource(resolver, path, (String) null, "sling:Folder", false);
            if (properties != null) {

                // add the properties to the store resource, using the given resolver

                ModifiableValueMap currentProps = storedResource.adaptTo(ModifiableValueMap.class);
                if (currentProps != null) {
                    Set<String> names = properties.keySet();
                    for (String name : names) {
                        currentProps.put(name, properties.get(name));
                    }
                    resolver.commit();
                    log.info("Properties stored with user '{}' below {}", resolver.getUserID(), path);
                    return true;
                }
            } else {

                // wrap the request with the given resolver and the store resource

                HtmlResponse response = new HtmlResponse();
                FormStoreRequestWrapper requestWrapper = new FormStoreRequestWrapper(request, resolver, storedResource);

                // store the request data to the store resource, using the Sling modify operation on the wrapped request

                modifyOperation.run(requestWrapper, response, null);
                if (response.isSuccessful()) {
                    log.info("Properties stored with user '{}' below {}", resolver.getUserID(), path);
                    return true;
                }
            }
        } catch (PersistenceException e) {
            log.error("Cannot store the properties with user '{}': {}", resolver.getUserID(), e.getMessage());
            return false;
        }
        log.error("Cannot store the properties with user '{}' below {}", resolver.getUserID(), path);
        return false;
    }

    /*
     * Request wrapper class used to make the existing request rely on a different resource resolver
     * and on a different resource.
     */
    private class FormStoreRequestWrapper extends SlingHttpServletRequestWrapper {

        private ResourceResolver resolver;
        private Resource resource;

        public FormStoreRequestWrapper(SlingHttpServletRequest request, ResourceResolver resolver, Resource resource) {
            super(request);
            this.resolver = resolver;
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }

        public ResourceResolver getResourceResolver() {
            return resolver;
        }

    }

    /*
     * Returns the path of the closest existing node by walking up the ancestor tree
     */
    private static String findExistingPath(String path, Session session)
            throws RepositoryException {
        // the path should be absolute
        if (!path.startsWith("/")) {
            return null;
        }
        String tmp = path;
        while (tmp.length() > 0) {
            // return when first existing node is found
            if (session.nodeExists(tmp)) {
                return tmp;
            }
            if (tmp.equals("/")) {
                return null;
            }
            // take the parent path
            tmp = Text.getRelativeParent(tmp, 1);
        }
        return null;
    }

}

