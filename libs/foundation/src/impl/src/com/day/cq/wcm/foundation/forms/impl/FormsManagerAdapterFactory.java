/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2012 Adobe Systems Incorporated
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

import com.day.cq.wcm.foundation.forms.FormsManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Provides adaptability for the {@link FormsManager}.
 */
@Component
@Service
public class FormsManagerAdapterFactory implements AdapterFactory {

    @Property(name = "adapters")
    public static final String[] ADAPTERS = {
            FormsManager.class.getName(),
    };

    @Property(name = "adaptables")
    public static final String[] ADAPTABLES = {
            ResourceResolver.class.getName()
    };

    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
        if (adaptable instanceof ResourceResolver) {
            return (AdapterType) new FormsManagerImpl((ResourceResolver) adaptable);
        }
        return null;
    }

}
