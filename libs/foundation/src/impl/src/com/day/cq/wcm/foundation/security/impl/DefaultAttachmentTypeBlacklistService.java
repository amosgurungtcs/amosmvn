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
 *************************************************************************/
package com.day.cq.wcm.foundation.security.impl;

import com.day.cq.wcm.foundation.forms.attachments.AttachmentDataSource;
import com.day.cq.wcm.foundation.security.AttachmentTypeBlacklistService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(label = "%security.defaultattachmenttypeblacklistservice.name", metatype = true,
        configurationPid = "com.day.cq.wcm.foundation.security.DefaultAttachmentTypeBlacklistService")
@Service
public class DefaultAttachmentTypeBlacklistService implements AttachmentTypeBlacklistService {

    private final static Logger log = LoggerFactory.getLogger(DefaultAttachmentTypeBlacklistService.class);

    @Property(value = {".ade", ".adp", ".app", ".asa", ".asp", ".bas", ".bat", ".cer", ".chm", ".cmd", ".com",
        ".cpl", ".crt", ".csh", ".dll", ".exe", ".fxp", ".hlp", ".hta", ".htm", ".html", ".htr", ".inf", ".ins",
        ".isp", ".its", ".js", ".jse", ".ksh", ".lnk", ".mad", ".maf", ".mag", ".mam", ".maq", ".mar", ".mas",
        ".mat", ".mau", ".mav", ".maw", ".mda", ".mdb", ".mde", ".mdt", ".mdw", ".mdz", ".mht", ".mhtm", ".mhtml",
        ".msc", ".msi", ".msp", ".mst", ".ocx", ".ops", ".pcd", ".pif", ".prf", ".prg", ".reg", ".scf", ".scr",
        ".sct", ".shb", ".shs", ".tmp", ".url", ".vb", ".vbe", ".vbs", ".vbx", ".vsmacros", ".vss", ".vst", ".vsw",
        ".ws", ".wsc", ".wsf", ".wsh", ".xhtml", ".xsl"})
    public static final String PROPERTY_ATTACHMENT_TYPE_BLACKLIST = "default.attachment.type.blacklist";

    @Property(value = {".php", ".shtml", ".svg", ".swf", ".xml", ".xaml"})
    public static final String PROPERTY_ATTACHMENT_TYPE_BASELINE_BLACKLIST = "baseline.attachment.type.blacklist";

    private String[] attachmentTypeBlacklist;
    private List<String> configuredBaselineAttachmentTypeBlacklist;

    protected void activate(final ComponentContext context) {
        String[] rawArray = (String[]) context.getProperties().get(PROPERTY_ATTACHMENT_TYPE_BLACKLIST);
        final List<String> configuredBlackList =
            rawArray != null ? Arrays.asList(rawArray) : Collections.<String>emptyList();

        rawArray = (String[]) context.getProperties().get(PROPERTY_ATTACHMENT_TYPE_BASELINE_BLACKLIST);
        configuredBaselineAttachmentTypeBlacklist =
            rawArray != null ? Arrays.asList(rawArray) : Collections.<String>emptyList();

        final List<String> blackList = new ArrayList<String>();
        blackList.addAll(configuredBlackList);
        blackList.addAll(configuredBaselineAttachmentTypeBlacklist);

        attachmentTypeBlacklist = blackList.toArray(new String[blackList.size()]);

    }

    public String[] getBlacklist(final String[] overrideBlackList) {
        if (overrideBlackList != null) {
            if (overrideBlackList.length == 1 && DEFAULT.equals(overrideBlackList[0])) {
                return attachmentTypeBlacklist;
            } else {
                final ArrayList<String> result = new ArrayList<String>();
                result.addAll(Arrays.asList(overrideBlackList));
                result.addAll(configuredBaselineAttachmentTypeBlacklist);
                return result.toArray(new String[result.size()]);
            }
        } else {
            return attachmentTypeBlacklist;
        }
    }

    public boolean reject(final AttachmentDataSource attachmentDataSource) {
        final String[] blacklist = getBlacklist(null);

        final String contentFileType = attachmentDataSource.getTypeFromFileName();
        final String contentType = attachmentDataSource.getType();
        if (StringUtils.isNotBlank(contentFileType) && !ArrayUtils.contains(blacklist, contentFileType)) {
            if (StringUtils.isNotBlank(contentType) && !ArrayUtils.contains(blacklist, contentType)) {
                return false;
            } else {
                if (StringUtils.isNotBlank(contentType)) {
                    log.info("File of type " + contentType
                            + " is blacklisted for security reasons. To upload edit the"
                            + " servlet's attachment black list");
                } else {
                    log.info("Tika couldn't figure out the file type. Not attaching it as" + " it is suspicious");
                }
                return true;
            }
        } else {
            if (StringUtils.isNotBlank(contentFileType)) {
                log.info("File of type " + contentFileType + "with name " + attachmentDataSource.getName()
                        + " is blacklisted for security reasons. To upload edit the"
                        + " servlet's attachment black list");
            } else {
                try {
                    if (StringUtils.isEmpty(attachmentDataSource.getName())
                            && (attachmentDataSource.getInputStream() == null) || attachmentDataSource.getSize() == 0) {
                        log.debug("Attachment name is empty and the input stream is null or size is zero, allowing");
                        return false;
                    }
                } catch (final IOException e) {
                    log.info("Could not examine input stream's size, not attaching.", e);
                }
                log.info("File doesn't have a valid extension. Not attaching it as it is suspicious");
            }
            return true;
        }
    }
}
