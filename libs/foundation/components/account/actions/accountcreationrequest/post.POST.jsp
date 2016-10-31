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
  Handles request to create a new account


--%><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"
          import="java.util.HashMap,
                  java.util.Iterator,
                  java.util.Map,
                  org.apache.sling.api.request.RequestParameter,
                  org.apache.sling.api.request.RequestParameterMap,
                  com.day.cq.wcm.foundation.forms.FormsHelper,
                  com.adobe.cq.account.api.AccountManagementService" %><%
%><%!

    final String USER_ID = "rep:userId";
    final String PASSWORD = "rep:password";
    final String PF_REP = "rep:";
    final String PF_CQ = "cq:";

    private Map<String, RequestParameter[]> filterParameter(Iterator<Resource> itr, RequestParameterMap paras) {
        Map<String, RequestParameter[]> prefs = new HashMap<String, RequestParameter[]>();
        while(itr.hasNext()) {
            String name = FormsHelper.getParameterName(itr.next());
            // filter all rep and cq properties
            if (!paras.containsKey(name)
                    || name.startsWith("_")
                    || name.startsWith(PF_REP)
                    || name.startsWith(PF_CQ)) {
                continue;
            }
            prefs.put(name, paras.getValues(name));
        }
        return prefs;
    }

%><%

    boolean success = false;
    final String userId = request.getParameter(USER_ID) == null ? null : slingRequest.getRequestParameter(USER_ID).getString();
    final String pwd = request.getParameter(PASSWORD) == null ? null : slingRequest.getRequestParameter(PASSWORD).getString();
    Map<String, RequestParameter[]> userProps = filterParameter(FormsHelper.getFormElements(resource), slingRequest.getRequestParameterMap());
    String requestUrl = request.getRequestURL().toString();
    String configPath = resource.getPath();

    AccountManagementService accountManagementService = sling.getService(AccountManagementService.class);
    if (accountManagementService != null) {
        try {
            success = accountManagementService.requestAccount(userId, pwd, userProps, requestUrl, configPath);
        } catch (Exception e) {
            log.error("Error requesting a new account: {}", e);
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