<%@include file="/libs/foundation/global.jsp"%>
<h3>Change Your Password:</h3>
<form>
	<b>Old password:</b><br />
	<input type="password" class="form-control" id="oldpassword" maxlength="50">
	<b>New password:</b><br />
	<input type="password" class="form-control" id="newpassword"  maxlength="50">
	<b>Confirm new password:</b><br />
	<input type="password" class="form-control" id="confirmpassword"  maxlength="50">
	<br />
	<input type = "submit" class="btn btn-warning" value="Change Password">		
</form>