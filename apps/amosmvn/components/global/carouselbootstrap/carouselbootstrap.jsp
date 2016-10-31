<%@include file="/libs/foundation/global.jsp"%>
<% 
String[] img = properties.get("images", String[].class);
String[] captions = properties.get("captions", String[].class);

%>

<div id="recomendedCarousel" class="carousel slide" data-ride="carousel">
		<!-- Indicators -->
		<ol class="carousel-indicators">


            <%
            if(img!=null){
                for(int i =0;i <img.length;i++){ 
                    if(i==0){%>
            			<li data-target="#recomendedCarousel" data-slide-to="0" class="active"></li>
					<%
                    }
                    else { %>
            			<li data-target="#recomendedCarousel" data-slide-to="<%= i %>"></li>
                    <%
                    }
                }
			} %>
		</ol>

		<!-- Wrapper for slides -->
		<div class="carousel-inner" role="listbox">
            <%
    		if(img!=null){
                for(int i =0;i <img.length;i++){ 
                    if(i==0){%>
                        <div class="item active">
                            <img src="<%= img[i] %>" alt="<%= img[i] %>" >
                            <%if (captions!=null){ %>
                                <div class="carousel-caption">
                                    <p> <%=captions[i]%> </p>
                                </div>
                            <%}%>
                        </div>
                    <%
                    }
                    else{ %>
                        <div class="item">
                            <img src="<%= img[i] %>" alt="<%= img[i] %>" >
                            <%if (captions!=null){
                        	if(captions.length > i){%>
                                <div class="carousel-caption">
                                    <p> <%=captions[i]%> </p>
                                </div>
                            <%	}
                            }%>
                        </div>
                    <%
                    }
                } %>
            <%} else{%>
				<div class="item active">
                    <img src="dummy.png" >
                 </div>
			<%}%>
		</div>

		<!-- Left and right controls -->
		<a class="left carousel-control" href="#recomendedCarousel" role="button"
			data-slide="prev"> <span class="glyphicon glyphicon-chevron-left"
			aria-hidden="true"></span> <span class="sr-only">Previous</span>
		</a> <a class="right carousel-control" href="#recomendedCarousel" role="button"
			data-slide="next"> <span
			class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
			<span class="sr-only">Next</span>
		</a>
	</div>
