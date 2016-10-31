<%@include file="/libs/foundation/global.jsp"%>
<%
    String baseURL = currentNode.getPath()+".json";
%>
	<div class="login-card">
	    <h1>Log-in</h1><br>
	  	<form action ="<%= baseURL %>" method = "GET">
		    <input type="text" name="username" placeholder="Username">
		    <input type="password" name="password" placeholder="Password">
		    <input type="submit" name="login" class="login login-submit" value="login">
	 	 </form>

		  <div class="login-help">
              <a href="/content/Amos/register.html">Register</a>
		  </div>
	</div>
