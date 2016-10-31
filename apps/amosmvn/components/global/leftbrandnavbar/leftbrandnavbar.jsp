<%@include file="/libs/foundation/global.jsp"%>

<%
	String[] brands = currentStyle.get("brands", String[].class);
	String[] links = currentStyle.get("links", String[].class);
	String[] brandlinks = new String[100];
    if(brands!=null){
        if(links!=null){
        	System.arraycopy(links, 0, brandlinks, 0, links.length);
        }
        for(int i =0; i<brandlinks.length; i++){
            if(brandlinks[i]==null){
                brandlinks[i] ="#";
            }
        }
    }
%>			
			<div class="list-group table-of-contents">
                <%if(brands!= null){
                    for(int i =0; i< brands.length;i++){  %>
              			<a class="list-group-item" href="<%=brandlinks[i]%>.html"><%=brands[i]%></a>
                    <%}
                }%>
            </div>