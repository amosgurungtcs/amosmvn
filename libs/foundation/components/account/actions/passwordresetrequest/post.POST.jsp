<%--
  ADOBE CONFIDENTIAL
  ___________________

   Copyright 2015 Adobe Systems Incorporated
   All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.

  ==============================================================================

  Form 'action' component
  Handles request to reset a user password

--%><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"
          import="java.util.HashMap,
                  com.day.cq.wcm.foundation.forms.FormsHelper,
                  com.adobe.cq.account.api.AccountManagementService" %><%
%><%

    boolean success = false;
    final String userId = request.getParameter("rep:userId") == null ? null : slingRequest.getRequestParameter("rep:userId").getString();
    String requestUrl = request.getRequestURL().toString();
    String configPath = resource.getPath();

    AccountManagementService accountManagementService = sling.getService(AccountManagementService.class);
    if (accountManagementService != null) {
        try {
            success = accountManagementService.requestPasswordReset(userId, requestUrl, configPath);
        } catch (RepositoryException e) {
            log.error("Error requesting a password request");
        }
    } else {
        log.error("The AccountManagementService is not defined");
    }

    String path = properties.get("thankYouPage", "");
    if ("".equals(path) || !success) {
        FormsHelper.redirectToReferrer(slingRequest, slingResponse, new HashMap<String, String[]>());
    } else {
        if (!path.contains(".")) {
            path += ".html"; 
        }
        response.sendRedirect(slingRequest.getResourceResolver().map(request, path));
    }
%>