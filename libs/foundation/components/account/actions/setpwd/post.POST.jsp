<%--
  Copyright 1997-2010 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

--%><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"
            import="java.util.HashMap,
                    javax.jcr.RepositoryException,
                    org.apache.commons.lang.StringUtils,
                    org.apache.jackrabbit.api.JackrabbitSession,
                    org.apache.jackrabbit.api.security.user.UserManager,
                    org.apache.jackrabbit.api.security.user.User,
                    org.apache.sling.api.request.RequestParameter,
                    com.day.cq.wcm.foundation.forms.FormsHelper" %><%
%><%

    final String key = request.getParameter("ky") == null ? null : slingRequest.getRequestParameter("ky").getString();
    final String uid = request.getParameter("uid") == null ? null : slingRequest.getRequestParameter("uid").getString();

    boolean tokenBasedReset = StringUtils.isNotEmpty(key);
    boolean pwdChanged = false;

    if (tokenBasedReset) {

%><sling:include path="<%= currentPage.getPath() %>" resourceType="security/accountmgr/confirm"/><%

        pwdChanged = (Boolean) request.getAttribute("cq.account.operationStatus");

    } else {

        RequestParameter pwd = slingRequest.getRequestParameter("passwordreset");

        // Current password parameter is optional. This is to be compatible with < 6.0
        // New developments must send current password
        RequestParameter currentPwd = slingRequest.getRequestParameter("currentPwd");

        JackrabbitSession session = (JackrabbitSession) resourceResolver.adaptTo(javax.jcr.Session.class);
        try {
            UserManager userManager = session.getUserManager();
            String userId = (uid == null) ? resourceResolver.getUserID() : uid;
            User user = (User) userManager.getAuthorizable(userId);
            if (user != null) {
                if (currentPwd == null || StringUtils.isEmpty(currentPwd.toString())) {
                    // We no longer allow password change without the current password See CQ-18230
                    // This introduces an intentional backwards incompatibility.
                    log.warn("Security violation, request to update password without providing the existing password");
                } else {
                    user.changePassword(pwd.toString(), currentPwd.toString());
                    if (!userManager.isAutoSave()) {
                        session.save();
                    }
                    pwdChanged = true;
                    log.info("Password changed for user [ {} ]", userId);
                }
            } else {
                log.error("Failed to locate user [ {} ]", userId);
            }
        } catch (RepositoryException e) {
            log.error("Failed to reset password", e);
            try {
                session.refresh(false);
            } catch (RepositoryException re) {
                log.error("Failed to reset password", re);
            }
        }
    }

    if (pwdChanged) {
        String path = properties.get("pwdChangedPage", "");
        if (StringUtils.isEmpty(path)) {
            log.warn("Success page 'pwdChangedPage' is not defined. Redirecting to the referrer");
            FormsHelper.redirectToReferrer(slingRequest, slingResponse, new HashMap<String, String[]>());
        } else {
            path = slingRequest.getResourceResolver().map(request, path);
            // are we really suppose to fix the success page?
            if (!StringUtils.contains(path, ".html")) {
                path += ".html";
            }
            response.sendRedirect(path);
        }
    } else {
        log.error("Failed to reset password. Redirecting to the referrer");
        // Currently there is no defined way to report server error.
        // until 5.6, reset failure was not reported and request was redirected to the success page.
        // adding a simple error parameter. This should probably be a part of FormsHelper
        final HashMap<String, String[]> params = new HashMap<String, String[]>();
        params.put("error", new String[]{"Could not set Password"});
        FormsHelper.redirectToReferrer(slingRequest, slingResponse, params);
    }

%>