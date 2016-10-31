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
package com.day.cq.wcm.foundation.security.impl;

import com.day.cq.wcm.foundation.forms.attachments.AttachmentDataSource;
import com.day.cq.wcm.foundation.security.AttachmentTypeBlacklistService;
import com.day.cq.wcm.foundation.security.SaferSlingPostValidator;
import com.day.text.Text;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The <code>SaferSlingPostValidatorImpl</code> checks Sling POST requests for safety and recommends to REJECT or
 * ACCEPT based on safe/unsafe constructs in the request.
 */
@Component(label = "%security.saferslingpostvalidator.name",
        description = "%security.saferslingpostvalidator.description",
        metatype = true,
        configurationPid = "com.day.cq.wcm.foundation.security.SaferSlingPostValidatorImpl")
@Service(serviceFactory = true, value = SaferSlingPostValidator.class)
@Properties({@Property(name = "service.description", value = "Safer Sling Post Validator", propertyPrivate = true)})
public class SaferSlingPostValidatorImpl implements SaferSlingPostValidator {

    private static final String OPERATION_PARAMETER = ":operation";
    private static final String TYPEHINT_SUFFIX = "@TypeHint";
    private static final String RESOURCETYPE_PARAMETER = "sling:resourceType";
    private static final String RESOURCESUPERTYPE_PARAMETER = "sling:resourceSuperType";
    private static final String JCR_PRIMARYTYPE = "jcr:primaryType";
    private static final String JCR_MIXINTYPES = "jcr:mixinTypes";
    private static final String BINARY = "Binary";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Properties with : in them are often security significant, by default they are not allowed through, with certain
     * 'safe' values white listed. Properties starting with : in them are automatically white listed.
     */
    @Property(value = {"jcr:description", "jcr:title", "jcr:lastModified", JCR_PRIMARYTYPE, JCR_MIXINTYPES,
        RESOURCETYPE_PARAMETER, RESOURCESUPERTYPE_PARAMETER, "cq:tags"},
            label = "%security.saferslingpostvalidator.whitelist.name",
            description = "%security.saferslingpostvalidator.whitelist.description")
    private static final String PARAMETER_WHITELIST = "parameter.whitelist";

    /**
     * This configuration allows white listing certain parameter name prefixes which are known to be safe.
     */
    @Property(value = {},
            label = "%security.saferslingpostvalidator.prefix.name",
            description = "%security.saferslingpostvalidator.prefix.description",
            cardinality = Integer.MAX_VALUE)
    private static final String PARAMETER_WHITELIST_PREFIXES = "parameter.whitelist.prefixes";

    /**
     * Binary properties with . in them can trigger mime type guessing from the 'extension' part when served by the
     * DefaultGetServlet. Therefore such properties are excluded by default, with a white list which is empty by
     * default.
     */
    @Property(value = {},
            label = "%security.saferslingpostvalidator.binary.name",
            description = "%security.saferslingpostvalidator.binary.description",
            cardinality = Integer.MAX_VALUE)
    private static final String BINARY_PARAMETER_WHITELIST = "binary.parameter.whitelist";

    /**
     * The SlingPostServlet supports some powerful @ suffixes for parameter names to control copying from existing
     * content for example. But some of these suffixes allow bypassing security otherwise provided by the Dispatcher.
     * So only white listed suffixes are allowed.
     */
    @Property(value = {TYPEHINT_SUFFIX, "@DefaultValue", "@UseDefaultWhenMissing", "@IgnoreBlanks", "@ValueFrom",
        "@Delete", "@Patch"},
            label = "%security.saferslingpostvalidator.modifier.name",
            description = "%security.saferslingpostvalidator.modifier.description"
    )
    private static final String MODIFIER_WHITELIST = "modifier.whitelist";

    /**
     * The SlingPostServlet supports some powerful :operation values such as import/export. So only the basic safe
     * operations are allowed.
     */
    @Property(value = {"delete", "nop"},
            label = "%security.saferslingpostvalidator.operation.name",
            description = "%security.saferslingpostvalidator.operation.description"
    )
    private static final String OPERATION_WHITELIST = "operation.whitelist";

    /**
     * The SlingPostServlet supports some powerful :operation values such as import/export. So only specific
     * operations or operations implemented to be safe and given a specific name prefix are allowed.
     */
    @Property(value = {},
            label = "%security.saferslingpostvalidator.operation.prefix.name",
            description = "%security.saferslingpostvalidator.operation.prefix.description",
            cardinality = Integer.MAX_VALUE)
    private static final String OPERATION_WHITELIST_PREFIXES = "operation.whitelist.prefixes";

    /**
     * The TypeHint suffix does double duty: selecting property types and node types. Node types are significant
     * because the JCR/Sling observers often filter based on node type, and can place undue trust in the content of
     * nodes bearing the expected node type. Further node types set default sling:ResourceTypes which are used to
     * locate servlets. Allowing attackers to choose unexpected servlets increases the attack surface area
     * drastically. We allow all known property types, but a 'safe' subset of node types.
     */
    @Property(value = {"cq:CalendarEvent", "nt:unstructured", "nt:folder", "nt:file", "nt:resource", "sling:Folder",
        "sling:OrderedFolder", BINARY, "Boolean", "Date", "Double", "Long", "Name", "Path", "String", "String[]"},
            label = "%security.saferslingpostvalidator.typehint.name",
            description = "%security.saferslingpostvalidator.typehint.description"
    )
    private static final String TYPEHINT_WHITELIST = "typehint.whitelist";

    /**
     * Resource types are the means by which Sling selects which servlets to use to service requests. Allowing an
     * attacker full range to choose any existing servlet would drastically increase the attack surface, so by default
     * all resource type settings (sling:resourceType and sling:resourceSuperType) are rejected, except those known to
     * be needed by features. Candidates on the whitelist should be strongly vetted for XSS, CSRF and any other
     * security vulnerabilities.
     */
    @Property(value = {},
            label = "%security.saferslingpostvalidator.resourcetype.name",
            description = "%security.saferslingpostvalidator.resourcetype.description",
            cardinality = Integer.MAX_VALUE)
    private static final String RESOURCETYPE_WHITELIST = "resourcetype.whitelist";

    @Reference
    private AttachmentTypeBlacklistService attachmentTypeBlacklist;

    private Set<String> parameterWhiteList = new HashSet<String>();
    private Set<String> parameterWhiteListPrefixes = new HashSet<String>();
    private Set<String> binaryParameterWhiteList = new HashSet<String>();
    private Set<String> modifierWhiteList = new HashSet<String>();
    private Set<String> operationWhiteList = new HashSet<String>();
    private Set<String> operationWhiteListPrefixes = new HashSet<String>();
    private Set<String> typeHintWhiteList = new HashSet<String>();
    private Set<String> resourceTypeWhiteList = new HashSet<String>();

    /**
     * Grab all our configurations.
     * @param ctx our component context.
     */
    @Activate
    protected void activate(final ComponentContext ctx) {
        Object o = ctx.getProperties().get(PARAMETER_WHITELIST);
        if (o instanceof String[]) {
            parameterWhiteList = new HashSet<String>();
            parameterWhiteList.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(PARAMETER_WHITELIST_PREFIXES);
        if (o instanceof String[]) {
            parameterWhiteListPrefixes = new HashSet<String>();
            parameterWhiteListPrefixes.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(BINARY_PARAMETER_WHITELIST);
        if (o instanceof String[]) {
            binaryParameterWhiteList = new HashSet<String>();
            binaryParameterWhiteList.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(MODIFIER_WHITELIST);
        if (o instanceof String[]) {
            modifierWhiteList = new HashSet<String>();
            modifierWhiteList.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(OPERATION_WHITELIST);
        if (o instanceof String[]) {
            operationWhiteList = new HashSet<String>();
            operationWhiteList.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(OPERATION_WHITELIST_PREFIXES);
        if (o instanceof String[]) {
            operationWhiteListPrefixes = new HashSet<String>();
            operationWhiteListPrefixes.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(TYPEHINT_WHITELIST);
        if (o instanceof String[]) {
            typeHintWhiteList = new HashSet<String>();
            typeHintWhiteList.addAll(Arrays.asList((String[]) o));
        }
        o = ctx.getProperties().get(RESOURCETYPE_WHITELIST);
        if (o instanceof String[]) {
            resourceTypeWhiteList = new HashSet<String>();
            resourceTypeWhiteList.addAll(Arrays.asList((String[]) o));
        }
    }

    public boolean reject(final SlingHttpServletRequest request, final String... whitelistPatterns) {
        final Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final Object o = paramNames.nextElement();
            if (!(o instanceof String)) {
                return REJECT;
            }

            final String fullName = (String) o;

            if (parameterWhiteList.contains(fullName)) {
                continue;
            }

            // reject starting, ending or containing ../ or starting with /
            if (fullName.startsWith("../") || fullName.endsWith("/..") ||
                            fullName.contains("/../") || fullName.startsWith("/")) {
                return REJECT;
            }

            final String prename = leafName(fullName);
            final String name;

            final int atidx = prename.lastIndexOf('@');
            if (atidx >= 0) {
                final String modifier = prename.substring(atidx);
                name = prename.substring(0, atidx);
                if (checkModifier(request, name, fullName, modifier) != ACCEPT) {
                    return REJECT;
                }
            } else {
                name = prename;
            }

            if (OPERATION_PARAMETER.equals(name) && checkOperation(request, name, fullName) != ACCEPT) {
                return REJECT;
            }

            if ((JCR_PRIMARYTYPE.equals(name) || JCR_MIXINTYPES.equals(name))
                    && checkJCRTypes(request, name, fullName) != ACCEPT) {
                return REJECT;
            }

            if ((RESOURCETYPE_PARAMETER.equals(name) || RESOURCESUPERTYPE_PARAMETER.equals(name))
                    && checkResourceTypes(request, name, fullName) != ACCEPT) {
                return REJECT;
            }

            if (name.contains(":") && !name.startsWith(":") && !parameterWhiteList.contains(name)) {
                if (checkParameterPrefixes(name) != ACCEPT) {
                    if (checkParameterPatterns(name, whitelistPatterns) != ACCEPT) {
                        return REJECT;
                    }
                }
            }

            if (":applyTo".equals(name) && validateApplyTo(request) != ACCEPT) {
                return REJECT;
            }

            if (validateUploads(request, name) != ACCEPT) {
                return REJECT;
            }

        }
        return ACCEPT;
    }

    private boolean checkParameterPrefixes(final String name) {
        for (final String prefix : parameterWhiteListPrefixes) {
            if (name.startsWith(prefix)) {
                return ACCEPT;
            }
        }
        return REJECT;
    }

    private boolean checkParameterPatterns(final String name, final String... patterns) {
        if (patterns == null || patterns.length == 0) {
            return ACCEPT;
        }
        for (String patternVal : patterns) {
            try {
                Pattern p = Pattern.compile(patternVal);
                Matcher m = p.matcher(name);
                if (m.matches()) {
                    return ACCEPT;
                }
            } catch (PatternSyntaxException e) {
                logger.warn("invalid pattern [{}] provided to SaferSlingPostValidator: ", patternVal, e);
            }
        }
        return REJECT;
    }

    private String leafName(final String name) {
        final int slashidx = name.lastIndexOf('/');
        if (slashidx >= 0) {
            return name.substring(slashidx + 1);
        }
        return name;
    }

    private boolean checkResourceTypes(final SlingHttpServletRequest request, final String name, final String fullName) {
        final String[] values = request.getParameterValues(fullName);
        if (values != null) {
            if (values.length > 1) {
                return REJECT;
            }
            /**
             * empty is not allowed as it serves no purpose, and sling:resourceType is so sensitive I don't want to
             * allow even creating the property unless it is for a useful white listed value.
             */
            if (!resourceTypeWhiteList.contains(values[0])) {
                return REJECT;
            }
        } else {
            return REJECT;
        }
        return ACCEPT;
    }

    private boolean checkJCRTypes(final SlingHttpServletRequest request, final String name, final String fullName) {
        final String[] values = request.getParameterValues(fullName);
        if (values != null) {
            for (final String type : values) {
                if (!typeHintWhiteList.contains(type)) {
                    return REJECT;
                }
            }
        }
        return ACCEPT;
    }

    private boolean checkOperation(final SlingHttpServletRequest request, final String name, final String fullName) {
        final String[] values = request.getParameterValues(fullName);
        if (values != null) {
            if (values.length > 1) {
                return REJECT;
            }
            if ((!"".equals(values[0])) && !operationWhiteList.contains(values[0])) {
                for (final String prefix : operationWhiteListPrefixes) {
                    if (values[0].startsWith(prefix)) {
                        return ACCEPT;
                    }
                }
                return REJECT;
            }
        }
        return ACCEPT;
    }

    private boolean checkModifier(final SlingHttpServletRequest request, final String name, final String fullName,
        final String modifier) {
        if (!"".equals(modifier) && !modifierWhiteList.contains(modifier)) {
            return REJECT;
        }
        if (checkTypeHint(request, name, fullName, modifier) != ACCEPT) {
            return REJECT;
        }
        return ACCEPT;
    }

    private boolean checkTypeHint(final SlingHttpServletRequest request, final String name, final String fullName,
        final String modifier) {
        if (TYPEHINT_SUFFIX.equals(modifier)) {
            final String[] values = request.getParameterValues(fullName);
            if (values != null) {
                if (values.length > 1) {
                    return REJECT;
                }
                final String type = values[0];
                if ((!"".equals(type)) && !typeHintWhiteList.contains(type)) {
                    return REJECT;
                }
                if (BINARY.equals(type)) {
                    if (name.contains(".") && !binaryParameterWhiteList.contains(name)) {
                        return REJECT;
                    }
                }
            }
        }
        return ACCEPT;
    }

    private boolean validateApplyTo(final SlingHttpServletRequest request) {
        final String[] paths = request.getParameterValues(":applyTo");
        if (paths == null) {
            return ACCEPT;
        }
        final Integer depth = (Integer) request.getAttribute(SaferSlingPostValidator.POST_DEPTH_ATTRIBUTE);
        for (final String path : paths) {
            if (path != null
                    && validateApplyToPath(path, request.getResource().getPath(), depth != null ? depth : 0) != ACCEPT) {
                return REJECT;
            }
        }
        return ACCEPT;
    }

    private boolean validateApplyToPath(final String applyToPath, final String resourcePath, final int depth) {
        final String canonPath = Text.makeCanonicalPath(applyToPath);
        if (!canonPath.startsWith(resourcePath)) {
            return REJECT;
        }
        String testPath = canonPath.substring(resourcePath.length());
        if (testPath.length() > 0 && testPath.charAt(0) == '/') {
            testPath = testPath.substring(1);
        }
        if (testPath.length() == 0) {
            return ACCEPT;
        }
        final int testDepth = testPath.split("/").length;
        if (testDepth > depth) {
            return REJECT;
        }
        return ACCEPT;
    }

    private boolean validateUploads(final SlingHttpServletRequest request, final String name) {
        final RequestParameter[] rps = request.getRequestParameters(name);
        if (rps == null) {
            return ACCEPT;
        }
        for (final RequestParameter rp : rps) {
            if (!rp.isFormField()) {
                final AttachmentDataSource ads = new AttachmentDataSource(rp);
                if (attachmentTypeBlacklist.reject(ads)) {
                    return REJECT;
                }
            }
        }
        return ACCEPT;
    }

}
