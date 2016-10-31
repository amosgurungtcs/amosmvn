<%@include file="/libs/foundation/global.jsp"%>
<cq:includeClientLib categories="loginstatus" />
<%
    String homepageheader = currentStyle.get("homepageheader",currentPage.getTitle());
	String homelink = currentStyle.get("homelink","#");

	String category1header = currentStyle.get("category1header",currentPage.getTitle());
	String category2header = currentStyle.get("category2header",currentPage.getTitle());

	String brand1cat1 = currentStyle.get("brand1cat1",currentPage.getTitle());
	String brand2cat1 = currentStyle.get("brand2cat1",currentPage.getTitle());
	String brand3cat1 = currentStyle.get("brand3cat1",currentPage.getTitle());
    String brand4cat1 = currentStyle.get("brand4cat1",currentPage.getTitle());
    String brand5cat1 = currentStyle.get("brand5cat1",currentPage.getTitle());

	String link1cat1 = currentStyle.get("link1cat1","#");
	String link2cat1 = currentStyle.get("link2cat1","#");
	String link3cat1 = currentStyle.get("link3cat1","#");
	String link4cat1 = currentStyle.get("link4cat1","#");
	String link5cat1 = currentStyle.get("link5cat1","#");


	String brand1cat2 = currentStyle.get("brand1cat2",currentPage.getTitle());
	String brand2cat2 = currentStyle.get("brand2cat2",currentPage.getTitle());
	String brand3cat2 = currentStyle.get("brand3cat2",currentPage.getTitle());

	String link1cat2 = currentStyle.get("link1cat2","#");
	String link2cat2 = currentStyle.get("link2cat2","#");
	String link3cat2 = currentStyle.get("link3cat2","#");

	HttpSession session = slingRequest.getSession();
    %>

    <nav class="navbar navbar-default navbar-static-top">
	  <div class="container-fluid">
	    <ul class="nav navbar-nav">
    	  <li class="active">
              <a href="<%= homelink %>.html"> <%= homepageheader %> </a>
          </li>
	      <li class="dropdown">
	          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><%=category1header%> <span class="caret"></span></a>
	          <ul class="dropdown-menu">
	            <li><a href="<%= link1cat1 %>.html"><%= brand1cat1 %></a></li>
                <li role="separator" class="divider"></li>
	            <li><a href="<%= link2cat1 %>.html"><%= brand2cat1 %></a></li>
	            <li><a href="<%= link3cat1 %>.html"><%= brand3cat1 %></a></li>
                <li><a href="<%= link4cat1 %>.html"><%= brand4cat1 %></a></li>
                <li><a href="<%= link5cat1 %>.html"><%= brand5cat1 %></a></li>
	          </ul>
          </li>
		  <li class="dropdown">
	          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><%=category2header%> <span class="caret"></span></a>
	          <ul class="dropdown-menu">
	            <li><a href="<%= link1cat2 %>.html"><%= brand1cat2 %></a></li>
	            <li><a href="<%= link2cat2 %>.html"><%= brand2cat2 %></a></li>
	            <li><a href="<%= link3cat2 %>.html"><%= brand3cat2 %></a></li>
	          </ul>
          </li>
            <script>
                console.log("SS: "+sessionStorage.getItem("username"));
                console.log("Cart: "+sessionStorage.getItem("cartcount"));
            </script>
	    </ul>
            <ul id="notlogedin" class="nav navbar-nav navbar-right">
                <!--request.getSession().getId() -->
                <li><a href="/content/Amos/register.html"  id="singup"> <span class="glyphicon glyphicon-user"></span> Signup</a></li>
                <li><a href="/content/Amos/signon.html"  id="login"> <span class="glyphicon glyphicon-log-in"></span> Login</a></li>
            </ul>

			<ul id="logedin" class="nav navbar-nav navbar-right">
                <li class ="navbar-text">Hello <span id="user"></span></li>
	 			<li class ="navbar-text"><cq:include path="logout_node" resourceType="Amos/components/global/signout" /></li>
            </ul>
	  </div>
	</nav>