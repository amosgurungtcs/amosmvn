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
package com.day.cq.wcm.foundation.forms;

import aQute.bnd.annotation.ProviderType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

/**
 * Service that runs the Form Store action.
 *
 * Originally the action was run in a jsp and had a few security flaws (use of an admin session,
 * temporary setting of privileges for anonymous, ...): see details in CQ-21784.
 * This service runs the action in a secure way and uses a service user (see details below).
 *
 */
@ProviderType
public interface FormStoreService {

    /**
     * Runs the Form Store action (defined at '/libs/foundation/components/form/actions/store') as follows:
     * 1) If the request session has enough rights the data is stored with the request session.
     * 2) Otherwise the service checks if the request session has the marker privilege 'cq:storeUGC'.
     * If so, a service user is used to store the data.
     *
     * @param request The Sling request
     * @param response The Sling response
     * @return true if the data was stored, false otherwise
     */
    public boolean runFormStoreAction(SlingHttpServletRequest request, SlingHttpServletResponse response);

}
