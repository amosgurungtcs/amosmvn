/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2015 Adobe Systems Incorporated
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
 *
 **************************************************************************/
package com.day.cq.wcm.foundation.impl.templated;

import com.day.cq.wcm.api.TemplateManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.cq.wcm.foundation.TemplatedContainer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Component
@Service(value = AdapterFactory.class)
@Properties({
        @Property(name = "service.description", value = "WCM Templated Container Adapter Factory"),
        @Property(name = "adapter.condition", value = "Adapts Request to TemplatedContainer")
})
public class TemplatedContainerAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(TemplatedContainerAdapterFactory.class);

    private static final Class<SlingHttpServletRequest> REQUEST_CLASS = SlingHttpServletRequest.class;

    private static final Class<TemplatedContainer> TEMPLATED_CONTAINER_CLASS = TemplatedContainer.class;

    @Property(name = AdapterFactory.ADAPTER_CLASSES)
    protected static final String[] ADAPTER_CLASSES = {
            TEMPLATED_CONTAINER_CLASS.getName()
    };

    @Property(name = AdapterFactory.ADAPTABLE_CLASSES)
    protected static final String[] ADAPTABLE_CLASSES = {
            REQUEST_CLASS.getName()
    };

    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return getAdapter((SlingHttpServletRequest) adaptable, type);
        }
        log.warn("Unable to handle adaptable {}", adaptable.getClass().getName());
        return null;
    }

    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(SlingHttpServletRequest request, Class<AdapterType> type) {
        if (request != null) {
            ResourceResolver resourceResolver = request.getResourceResolver();
            TemplateManager templateManager = resourceResolver.adaptTo(TemplateManager.class);
            ComponentContext componentContext = WCMUtils.getComponentContext(request);
            if (type == TEMPLATED_CONTAINER_CLASS && componentContext != null && templateManager != null) {
                return (AdapterType) new TemplatedContainer(templateManager, componentContext);
            }
        }
        log.warn("Unable to adapt resource to type {}", type.getName());
        return null;
    }
}
