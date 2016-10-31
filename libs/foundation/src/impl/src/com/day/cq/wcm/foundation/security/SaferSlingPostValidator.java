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
 **************************************************************************/
package com.day.cq.wcm.foundation.security;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Service to check Sling Post requests for unsafe constructs.
 */
public interface SaferSlingPostValidator {
    /**
     * REJECT returned when a request should be rejected.
     */
    boolean REJECT = true;
    /**
     * ACCEPT returned when a request may be accepted.
     */
    boolean ACCEPT = false;

    /**
     * Attribute set on the request to indicate the depth under the target request which :applyTo should be allowed to
     * reference.
     */
    String POST_DEPTH_ATTRIBUTE = SaferSlingPostValidator.class.getName() + ".depth";

    /**
     * Reject any unsafe post requests.
     * @param request the request to check
     * @param whitelistPatterns additional whitelist patterns
     * @return REJECT if the request should be rejected, otherwise return ACCEPT.
     */
    boolean reject(SlingHttpServletRequest request, String... whitelistPatterns);

}
