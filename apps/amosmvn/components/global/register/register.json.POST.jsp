<%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"%><%
%><%@ page import="com.day.commons.datasource.poolservice.DataSourcePool" %><%
%><%@ page import="javax.sql.DataSource" %><%
%><%@ page import="java.sql.Connection" %><%
%><%@ page import="java.sql.SQLException" %><%
%><%@ page import="java.sql.Statement" %><%
%><%@ page import="java.sql.PreparedStatement" %><%
%><%@ page import="java.sql.ResultSet"%><%
%>
<%
    log.info("Entered into POST SERVLET");

		String username =  request.getParameter("username");
		String password = request.getParameter("password");
		int hashpassword = password.hashCode();
%> <%=username %> <%=hashpassword %> <%
	DataSourcePool dspService = sling.getService(DataSourcePool.class);
	Connection c= null;
	PreparedStatement s = null;
  try {
     DataSource ds = (DataSource) dspService.getDataSource("amosmvnjdbcds");   
     if(ds != null) {
         %><p>Obtained the datasource!</p><%
         %><%
          c = ds.getConnection();
          String query = "Insert INTO users (username, hashpassword) values(?,?)";
          s = c.prepareStatement(query);
          s.setString(1,username);
          s.setInt(2,hashpassword);
          int num =s.executeUpdate();
		  HttpSession session = request.getSession();
		  session.setAttribute("username",username);

         response.sendRedirect("/content/Amos.html");
      } 
   }catch (Exception e) {
        %><p>error! <%=e.getMessage()%></p>
			<script>
                alert("Error: UserName already exists.");
                window.location.replace("/content/Amos/register.html");
      		</script><%
  }finally{
      if(s!=null){
          try{
              s.close();
          }catch(Exception e){
              %><p>error at statement close: <%=e.getMessage()%></p><%
          }
      }
      if(c!=null){
          try{
                c.close();
          }catch (Exception e){
            %><p>error at connection close! <%=e.getMessage()%></p><%
          }
      }
  }
%>