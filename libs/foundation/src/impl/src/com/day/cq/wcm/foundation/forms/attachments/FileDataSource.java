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

package com.day.cq.wcm.foundation.forms.attachments;

import javax.activation.DataSource;

/**
 * Provides type and size information from DataSource
 */
public interface FileDataSource extends DataSource {

    /**
     * Returns the MIME type of the content.
     * @return content MIME type.
     */
    String getType();

    /**
     * Returns the MIME type extension from file name.
     * @return content MIME type extension from file Name.
     */
    String getTypeFromFileName();

    /**
     * Returns the size of the file in bytes.
     * @return size of file in bytes.
     */
    long getSize();
}
