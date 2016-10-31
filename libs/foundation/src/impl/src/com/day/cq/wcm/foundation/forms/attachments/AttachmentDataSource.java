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

import org.apache.sling.api.request.RequestParameter;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of <code>FileDataSource</code>.
 */
public class AttachmentDataSource implements FileDataSource {

    /**
     * file request parameter.
     */
    private final RequestParameter fileRequestParameter;

    /**
     * Constructor of <code>AttachmentDataSource</code>.
     * @param fileRequestParameter The file request parameter
     */
    public AttachmentDataSource(final RequestParameter fileRequestParameter) {
        this.fileRequestParameter = fileRequestParameter;
    }

    /**
     * @return content media type.
     */
    public String getContentType() {
        // return fileRequestParameter.getFileName();
        try {
            final TikaConfig config = TikaConfig.getDefaultConfig();
            final Detector detector = config.getDetector();

            final TikaInputStream stream = TikaInputStream.get(this.getInputStream());

            final Metadata metadata = new Metadata();
            metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, this.getName());
            final MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.getBaseType().toString();
        } catch (final IOException e) {
            return this.getName();
        }
    }

    /**
     * @return content MIME type.
     */
    public String getType() {
        // return fileRequestParameter.getFileName();
        try {
            final TikaConfig config = TikaConfig.getDefaultConfig();
            final Detector detector = config.getDetector();

            final TikaInputStream stream = TikaInputStream.get(this.getInputStream());

            final Metadata metadata = new Metadata();
            metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, this.getName());
            final MediaType mediaType = detector.detect(stream, metadata);
            final MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
            return mimeType.getExtension();
        } catch (final IOException e) {
            return this.getName();
        } catch (final MimeTypeException e) {
            return this.getName();
        }
    }

    /**
     * @return content MIME type extension from file Name.
     */
    public String getTypeFromFileName() {
        final String fileName = getName();
        final int index = fileName.lastIndexOf(".");
        if (index > -1) {
            return fileName.substring(index);
        } else {
            return null;
        }
    }

    /**
     * @return size of attachment in bytes.
     */
    public long getSize() {
        return fileRequestParameter.getSize();
    }

    /**
     * @return request parameter input stream.
     * @throws java.io.IOException if I/O error occurs.
     */
    public InputStream getInputStream() throws IOException {
        return fileRequestParameter.getInputStream();
    }

    /**
     * @return request parameter file name.
     */
    public String getName() {
        return fileRequestParameter.getFileName();
    }

    /**
     * @return request parameter output stream.
     * @throws java.io.IOException if I/O error occurs.
     */
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

}
