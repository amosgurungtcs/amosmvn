<%@include file="/libs/foundation/global.jsp"%>
<%
    String baseURL = currentNode.getPath()+".json";
	String delims = "[/]";
    String[] tokens = baseURL.split(delims);
    %><div class="row">
<div class="col-xs-12">
		<ul class="breadcrumb"><%
	for(int i=2; i<tokens.length-1; i++)
    {
        String newURL="";
        for(int j =1;j<i+1;j++)
        {
            newURL+="/";
			newURL+=tokens[j];
        }
        newURL+=".html";
        if(i!= 1){
            if(i==2){
                %><li> <a href="<%= newURL %>"><span class="glyphicon glyphicon-home"></span> Home</a></li><%
            }else{
                %><li><a href="<%= newURL %>"><%= tokens[i] %></a></li><%
            }
        }
    }
%></ul>
</div></div><%
%>