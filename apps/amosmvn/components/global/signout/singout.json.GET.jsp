<%@include file="/libs/foundation/global.jsp"%>
<%
	HttpSession session=request.getSession();
	if(session!=null)
    {
		session.invalidate();
    }
%>
<script>
	sessionStorage.clear();
</script>