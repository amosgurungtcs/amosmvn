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

--%><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"
          import="java.util.ResourceBundle,
                  java.util.Locale,
                  com.day.cq.i18n.I18n"%><%

%><sling:include resourceType="security/accountmgr/confirm"/><%

%><%

    String CREATE_ACCOUNT_OPERATION = "create-account";
    String CHANGE_PASSWORD_OPERATION = "change-password";

    final Locale pageLocale = currentPage.getLanguage(false);
    final ResourceBundle bundle = slingRequest.getResourceBundle(pageLocale);
    final I18n i18n = new I18n(bundle);
    String operation = (String) request.getAttribute("cq.account.operation");
    Boolean isSuccess = (Boolean) request.getAttribute("cq.account.operationStatus");
    boolean isNewAccountOperation= CREATE_ACCOUNT_OPERATION.equals(operation);
    boolean isPasswordResetOperation = CHANGE_PASSWORD_OPERATION.equals(operation);
    String statusMessage = "";
    if (isNewAccountOperation && isSuccess) {
        statusMessage = "Congratulations! Your new account has been created.";
    } else if (isNewAccountOperation && !isSuccess) {
        statusMessage = "An error happened ... Your account could not be created.";
    } else if (isPasswordResetOperation && isSuccess) {
        statusMessage = "Your password has been reset and the new password has been emailed to you.";
    } else if (isPasswordResetOperation && !isSuccess) {
        statusMessage = "An error happened ... Your password could not be reset.";
    } else {
        statusMessage = "An error happened ...";
    }

%><div class="account-confirmation"><%= xssAPI.filterHTML(i18n.get(statusMessage)) %></div><%
%>
