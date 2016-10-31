/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
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
package com.day.cq.wcm.foundation;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.TemplateManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class TemplatedContainer extends WCMUsePojo {

    public static final Logger log = LoggerFactory.getLogger(TemplatedContainer.class);

    private Page page;

    private Template template;

    private ComponentContext componentContext;

    private TemplateManager templateManager;

    public TemplatedContainer(TemplateManager templateManager, ComponentContext componentContext) {
        this.componentContext = componentContext;
        if (componentContext == null) {
            throw new IllegalArgumentException("componentContext is null");
        }

        if(templateManager == null){
            throw new IllegalArgumentException("templateManger is null");
        }
        this.templateManager = templateManager;

        this.page = componentContext.getPage();
        if (page == null) {
            throw new IllegalArgumentException("component's page is null");
        }

        this.template = page.getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("page has no template assigned");
        }

        if (!template.hasStructureSupport()) {
            throw new IllegalArgumentException("template has no structure support");
        }
    }

    @Override
    public void activate() throws Exception {
        this.componentContext = WCMUtils.getComponentContext(getRequest());
        if (componentContext == null) {
            throw new IllegalArgumentException("componentContext is null");
        }

        this.templateManager = getResourceResolver().adaptTo(TemplateManager.class);
        if(this.templateManager == null){
            throw new IllegalArgumentException("templateManger is null");
        }

        this.page = getResourcePage();
        if (this.page == null) {
            throw new IllegalArgumentException("component's page is null");
        }

        this.template = page.getTemplate();
        if (this.template == null) {
            throw new IllegalArgumentException("page has no template assigned");
        }

        if (!template.hasStructureSupport()) {
            log.warn("template has no structure support", componentContext.toString());
        }
    }

    public List<Resource> getStructureResources() {
        return templateManager.getStructureResources(componentContext);
    }

    public String getNewResourceType(){
        return "wcm/foundation/components/templatedcontainer/newcontainer";
    }

    public boolean hasStructureSupport() {
        return template.hasStructureSupport();
    }

}

