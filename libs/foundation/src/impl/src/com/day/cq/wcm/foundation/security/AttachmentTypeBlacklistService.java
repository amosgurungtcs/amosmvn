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
 *
 *************************************************************************/
package com.day.cq.wcm.foundation.security;

import com.day.cq.wcm.foundation.forms.attachments.AttachmentDataSource;

/**
 * A service to provide a sensible default list of file extensions to be blacklisted.
 */
public interface AttachmentTypeBlacklistService {
    static final String DEFAULT = "DEFAULT";

    /**
     * Returns the default blacklist, or the supplied override blacklist.
     * @param overrideBlacklist any existing blacklist which should override the default.
     * @return If overrideBlacklist is the single value "DEFAULT" then the blacklist configured for this service will
     *         be returned. Otherwise the overrideBlacklist will be returned.
     */
    String[] getBlacklist(String[] overrideBlacklist);

    /**
     * Checks the attachment against the blacklist.
     * @param attachmentDataSource the attachment to check.
     * @return true if the attachment should be rejected, false if the attachment is acceptable.
     */
    boolean reject(AttachmentDataSource attachmentDataSource);
}
