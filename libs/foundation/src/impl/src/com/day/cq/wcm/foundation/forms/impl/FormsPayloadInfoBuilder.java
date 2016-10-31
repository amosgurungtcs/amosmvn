/*
 * ************************************************************************
 *  *
 *  * ADOBE CONFIDENTIAL
 *  * __________________
 *  *
 *  *  Copyright 2015 Adobe Systems Incorporated
 *  *  All Rights Reserved.
 *  *
 *  * NOTICE:  All information contained herein is, and remains
 *  * the property of Adobe Systems Incorporated and its suppliers,
 *  * if any.  The intellectual and technical concepts contained
 *  * herein are proprietary to Adobe Systems Incorporated and its
 *  * suppliers and are protected by trade secret or copyright law.
 *  * Dissemination of this information or reproduction of this material
 *  * is strictly forbidden unless prior written permission is obtained
 *  * from Adobe Systems Incorporated.
 *  *************************************************************************
 */

package com.day.cq.wcm.foundation.forms.impl;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.model.WorkflowNode;
import com.adobe.granite.workflow.payload.PayloadInfo;
import com.adobe.granite.workflow.payload.PayloadInfoBuilder;
import com.adobe.granite.workflow.payload.PayloadInfoBuilderContext;
import com.adobe.granite.xss.ProtectionContext;
import com.adobe.granite.xss.XSSFilter;

/**
 * An implementation of {@link PayloadInfoBuilder} to provides details about payloads that are submitted form content
 */
@Component
@Service(value = PayloadInfoBuilder.class)
@Property(name = Constants.SERVICE_RANKING, intValue = -100, propertyPrivate = true)
public class FormsPayloadInfoBuilder implements PayloadInfoBuilder {
    private static final Logger log = LoggerFactory.getLogger(FormsPayloadInfoBuilder.class);
    private static final String FORM_PATH_PROPERTY = "formPath";
    private static final String FORM_START_RESOURCE_TYPE = "foundation/components/form/start";
    private static final String FORM_END_RESOURCE_TYPE = "foundation/components/form/end";
    private static final String DISABLE_WCM_EDIT_MODE = "?wcmmode=disabled";

    @Reference(policy = ReferencePolicy.STATIC)
    private XSSFilter xss;

    @Override
    public PayloadInfo getPayloadInfo(PayloadInfoBuilderContext context) {
        String payloadPath = context.getInboxItem().getContentPath();

        if (StringUtils.isBlank(payloadPath)) {
            payloadPath = context.getPayloadPath();
        }

        Resource res = context.getResourceResolver().getResource(payloadPath);

        if (res != null) {
            //handle form content

            Resource parent = res.getParent();
            if (parent != null) {
                ValueMap values = res.getValueMap();
                ValueMap parentValues = parent.getValueMap();
                String formPath = parentValues.get("formPath", String.class);
                //If the parent resource has a formPath we assume it is form content
                if (StringUtils.isNotBlank(formPath)) {
                    String path = res.getPath();
                    String browserPath = path + ".html";
                    String description = "";

                    Resource form = context.getResourceResolver().getResource(ResourceUtil.getParent(formPath));
                    String formStartName = ResourceUtil.getName(parentValues.get("formPath", ""));
                    Iterator<Resource> children = form.listChildren();

                    while (children.hasNext()) {
                        //find the form start, then build the description out of the first 3 form fields after
                        //form start
                        Resource child = children.next();
                        if (child.isResourceType(FORM_START_RESOURCE_TYPE) && child.getName().equals(formStartName)) {
                            description = getDescription(values, children);

                            PayloadInfo info = context.createPayloadInfo();
                            info.setPath(path)
                                    .setBrowserPath(browserPath)
                                    .setDescription(description);

                            return info;
                        }
                    }
                }
            }

            //handle form
            //For now just support what the old PathBuilder supported.  Which means only handle the case where an
            //InboxItem is present, and that InboxItem is an instanceof WorkItem
            if (context.getInboxItem() != null && context.getInboxItem() instanceof WorkItem) {
                WorkflowNode node = ((WorkItem) context.getInboxItem()).getNode();
                if (node != null) {
                    String formPath = node.getMetaDataMap().get("FORM_PATH", String.class);
                    if (formPath != null && formPath.startsWith("/")) {
                        String path = context.getInboxItem().getContentPath();
                        String browserPath = path + ".form.html" + formPath + DISABLE_WCM_EDIT_MODE;
                        PayloadInfo info = context.createPayloadInfo();
                        info.setPath(path)
                                .setBrowserPath(browserPath);

                        return info;
                    }
                }
            }

        }

        return null;
    }

    private String getDescription(ValueMap values, Iterator<Resource> children) {
        StringBuilder builder = new StringBuilder();
        int count = 0;

        while(children.hasNext() && count < 3) {
            Resource child = children.next();
            ValueMap fieldValues = child.getValueMap();

            if (child.isResourceType(FORM_END_RESOURCE_TYPE)) {
                //the form ended before three fields were found
                break;
            }

            if (!fieldValues.containsKey("name")) {
                //skip non-form field
                continue;
            }

            String name = fieldValues.get("name", "");

            builder.append("<br>")
                    .append(name)
                    .append(": ")
                    .append(xss.filter(ProtectionContext.PLAIN_HTML_CONTENT, values.get(name, "")));

            count++;
        }

        return builder.toString();
    }
}
