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
    log.info("AAA Entered into Save POST SERVLET");

		String username =  request.getParameter("username");
		String password = request.getParameter("password");
		String cartitems = request.getParameter("cartitems");
		String cartprices = request.getParameter("cartprices");
		String cartqnts = request.getParameter("cartqnts");
		String cartproductids = request.getParameter("cartproductids");

		int hashpassword = password.hashCode();
%> <%=username %> <%=hashpassword %> <%
	DataSourcePool dspService = sling.getService(DataSourcePool.class);
	Connection c= null;
	PreparedStatement s = null;
	ResultSet rs = null;
  try {
     DataSource ds = (DataSource) dspService.getDataSource("amosmvnjdbcds");   
     if(ds != null) {
         %><p>Obtained the datasource!</p><%
         %><%
          c = ds.getConnection();
          String query = "SELECT userid from users where username = ? and hashpassword = ?";
          s = c.prepareStatement(query);
          s.setString(1,username);
          s.setInt(2,hashpassword);
          rs=s.executeQuery();
          int userid = 0;
          String name;
          while(rs.next()){ 
				userid = rs.getInt("userid");
          }%>
         <script>
             console.log("username "+ "<%=username%>");
             console.log("pass "+ "<%=password%>");
			console.log("userid "+ "<%=userid%>");
             </script><%
          if(userid!=0)
          {
              query = "Delete from cart where username = ?";
                  s = c.prepareStatement(query);
                  s.setString(1,username);
                  s.executeUpdate();

              String[] cartArray = cartitems.split(";");
              String[] pricesArray = cartprices.split(";");
              String[] qntArray = cartqnts.split(";");
              String[] productidArray = cartproductids.split(";");
              for(int i =0;i<cartArray.length;i++){
                  query = "Insert INTO cart (username, item,price,qnt,productid) values(?,?,?,?,?)";
                  s = c.prepareStatement(query);
                  s.setString(1,username);
                  s.setString(2,cartArray[i]);
                  s.setString(3,pricesArray[i]);
                  s.setString(4,qntArray[i]);
                  s.setString(5,productidArray[i]);
                  s.executeUpdate();
          	  }
              %>
				<script>
                    console.log("Set: <%=username%>");
                    sessionStorage.setItem("username","<%=username%>");
                    window.location.replace("/content/Amos.html");
				</script>
			  <%
          }
         else{
			%><script>
                alert("Error: Invalid username or password.");
				window.location.replace("/content/Amos/cart.html");
      		</script><%
         }
      } 
   }catch (Exception e) {
        %><p>error! <%=e.getMessage()%></p><%
  }finally{
      if(rs!=null){
          try{
              rs.close();
          }catch(Exception e){
              %><p>error at resultset close: <%=e.getMessage()%></p><%
          }
      }
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