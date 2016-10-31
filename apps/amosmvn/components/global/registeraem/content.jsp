<%@include file="/libs/foundation/global.jsp"%>
<!-- need for POST method or else get 403 error -->
<cq:includeClientLib js="granite.csrf.standalone"/>
<%
    String baseURL = currentNode.getPath()+".json";
%>
	<div class="login-card">
	    <h1>Register</h1><br>
	  	<form action ="<%= baseURL %>" method="POST">
		    <input type="text" name="username" placeholder="Username">
		    <input type="password" name="password" placeholder="Password">
		    <input type="submit" name="Register" class="login login-submit" value="Register">
	 	 </form>

		  <div class="login-help">
              <a href="/content/Amos/signon.html">Sign On</a>
		  </div>
	</div>