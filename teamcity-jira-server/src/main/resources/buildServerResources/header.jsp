<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<c:url value="${publisherSettingsUrl}" var="settingsUrl"/>
<script type="text/javascript">
	PublisherFeature = {
		showPublisherSettings: function() {
			var url = '${settingsUrl}?publisherId=' + $('publisherId').value  + "&projectId=${projectId}";
			$j.get(url, function(xhr) {
				$j("#publisherProperties").html(xhr);
			});
			return false;
		}
	};
</script>
<tr>
	<th><label for="addComment">Add comment:&nbsp;<l:star/></label></th>
	<td>
		<props:checkboxProperty name="addComment" />
	</td>
</tr>
<tr>
	<th><label for="commentFormat">Comment format:&nbsp;<l:star/></label></th>
	<td>
		<props:textProperty name="commentFormat" className="stringField"/>
	</td>
</tr>
<tr>
	<th><label for="jiraKeys">Jira project keys (example JIRA):&nbsp;<l:star/></label></th>
	<td>
		<props:textProperty name="jiraKeys" className="stringField"/>
	</td>
</tr>
<tr>
	<th><label for="resolveVersion">Jira version (i.e. ufos-core-8.xx.0):&nbsp;<l:star/></label></th>
	<td>
		<props:textProperty name="resolveVersion" className="stringField"/>
	</td>
</tr>

<tr>
	<th><label for="jiraUrl">Jira URL:&nbsp;<l:star/></label></th>
	<td>
		<props:textProperty name="jiraUrl" className="stringField"/>
	</td>
</tr>
<tr>
	<th><label for="jiraUser">Jira user:&nbsp;<l:star/></label></th>
	<td>
		<props:textProperty name="jiraUser" className="stringField"/>
	</td>
</tr>
<tr>
	<th><label for="jiraPassword">Jira password:&nbsp;<l:star/></label></th>
	<td>
		<props:passwordProperty name="jiraPassword" />
	</td>
</tr>
<tr>
	<th><label for="transitionIssue">Transition issue:&nbsp;<l:star/></label></th>
	<td>
		<props:checkboxProperty name="transitionIssue" />
	</td>
</tr>
<tr>
	<th><label for="transitionFormat">Transition format (FROM:TO,FROM:TO...):&nbsp;<l:star/></label></th>
	<td>
		<props:textProperty name="transitionFormat" className="stringField"/>
	</td>
</tr>
