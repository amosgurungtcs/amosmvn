<%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"%><%
%><%@ page import="com.day.commons.datasource.poolservice.DataSourcePool" %><%
%><%@ page import="javax.sql.DataSource" %><%
%><%@ page import="java.sql.Connection" %><%
%><%@ page import="java.sql.SQLException" %><%
%><%@ page import="java.sql.Statement" %><%
%><%@ page import="java.sql.PreparedStatement" %><%
%><%@ page import="java.sql.ResultSet"%><%
%><%@ page import="org.apache.jackrabbit.api.security.user.Authorizable"%><%
%><%@ page import="org.apache.jackrabbit.api.security.user.AuthorizableExistsException"%><%
%><%@ page import="org.apache.jackrabbit.api.security.user.Group"%><%
%><%@ page import="org.apache.jackrabbit.api.security.user.User"%><%    
%><%@ page import="org.apache.jackrabbit.api.security.user.UserManager"%>
<%
    log.info("AAA Entered into GET SERVLET");

		String username =  request.getParameter("username");
		String password = request.getParameter("password");
%> <%=username %> <%
  try {
		UserManager um = resourceResolver.adaptTo(UserManager.class);
		Session session = resourceResolver.adaptTo(Session.class);
		%> <%=um.getAuthorizable(username)%> <%
		if(um.getAuthorizable(username)==null){
			User user = um.createUser(username, password);	
		}
		session.save();
		%> <%=session.getUserID()%> <%
  }catch (Exception e) {
	  %> <%=e%> <%
  }finally{
	  
  }
%>