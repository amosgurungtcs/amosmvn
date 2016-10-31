<%@include file="/libs/foundation/global.jsp"%>
<%
	int col1width = properties.get("col1width",4);
	int col2width = properties.get("col2width",4);
	int col3width = properties.get("col3width",4);
%>
<div class="container">
    <div class="row">
        <div class ="col-xs-12 col-sm-<%=col1width%>">
            <cq:include path="left-component" resourceType="foundation/components/parsys" />
        </div>
        <div class ="col-xs-12 col-sm-<%=col2width%>">
            <cq:include path="center-component" resourceType="foundation/components/parsys" />
        </div>
        <div class ="col-xs-12 col-sm-<%=col3width%>">
            <cq:include path="right-component" resourceType="foundation/components/parsys" />
        </div>
	</div>
</div>
