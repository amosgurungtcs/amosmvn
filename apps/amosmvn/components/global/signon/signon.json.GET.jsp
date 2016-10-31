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
    log.info("AAA Entered into GET SERVLET");

		String username =  request.getParameter("username");
		String password = request.getParameter("password");
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
          HttpSession session = slingRequest.getSession();
          while(rs.next()){ 
				userid = rs.getInt("userid");
          } 
          if(userid!=0)
          {
              session.setAttribute("userid",userid);
              session.setAttribute("username",username);
			  String cart = new String();
              String pricecart = new String();
              String qntcart = new String();
              String productidcart = new String();
              int count =0;
              query = "SELECT item, price,qnt,productid from cart where username = ?";
          	  s = c.prepareStatement(query);
          	  s.setString(1,username);
          	  rs=s.executeQuery();
              while(rs.next()){ 
                  if(count !=0){
                      cart+=";";
                      pricecart+=";";
                      qntcart+=";";
                      productidcart+=";";
                  };

				cart += rs.getString("item");
                  pricecart += rs.getString("price");
                  qntcart +=rs.getString("qnt");
                  productidcart +=rs.getString("productid");
                count++;
          	  } 

              %>
				<script>
                    console.log("count: " +    <%=count%>);
                    console.log("cart ims: " + "<%=cart%>");

                    if(<%=count%>!=0){
                        var dbcart = "<%=cart%>";
                        var dbprice = "<%=pricecart%>";
                        var dbqnt = "<%=qntcart%>";
                        var dbproductid = "<%=productidcart%>"
                        var dbcount = <%=count%>;

                        if(sessionStorage.getItem("cartitems")!=null)
                        {
                            dbcart += sessionStorage.getItem("cartitems");
                            dbprice += sessionStorage.getItem("cartprices");
                            dbqnt += sessionStorage.getItem("cartqnts");
                            dbproductid += sessionStorage.getItem("cartproductids");
                            dbcount += parseInt(sessionStorage.getItem("cartcount"));

                            sessionStorage.setItem("cartitems",dbcart);
                            sessionStorage.setItem("cartprices",dbprice);
                            sessionStorage.setItem("cartqnts",dbqnt);
                            sessionStorage.setItem("cartproductids",dbproductid);
                            sessionStorage.setItem("cartcount",dbcount);
                        }else{
                            sessionStorage.setItem("cartitems",dbcart);
                            sessionStorage.setItem("cartprices",dbprice);
                            sessionStorage.setItem("cartqnts",dbqnt);
                            sessionStorage.setItem("cartproductids",dbproductid);
                            sessionStorage.setItem("cartcount",dbcount);
                        }
                    }
                    console.log("Set: <%=username%>");
                    sessionStorage.setItem("username","<%=username%>");
                    window.location.replace("/content/Amos.html");
				</script>
			  <%
          }
         else{
			%><script>
                alert("Error: Invalid username or password.");
				window.location.replace("/content/Amos/signon.html");
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