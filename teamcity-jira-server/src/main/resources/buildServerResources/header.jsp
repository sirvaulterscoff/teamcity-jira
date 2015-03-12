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
		showPublisherSettings: function () {
			var url = '${settingsUrl}?publisherId=' + $('publisherId').value + "&projectId=${projectId}";
			$j.get(url, function (xhr) {
				$j("#publisherProperties").html(xhr);
			});
			return false;
		}
	};

	$j(document).ready(function () {
		$j("#tab1").addClass("first selected").show();

		$j("ul.tabs li").click(function () {
			$j("ul.tabs li").removeClass("first selected");
			$j(this).addClass("first selected");
			$j(".tabContent").hide();
			var activeTab = $j(this).find("a").attr("href");
			$j(activeTab).fadeIn();
			return false;
		});
	});
</script>

<div class="simpleTabs" style="margin-bottom: 0.5em;">
	<ul class="tabs">
		<li class="first selected"><p><a href="#tab1">Common</a></p></li>
		<li class=""><p><a href="#tab2">Jira common</a></p></li>
		<li class=""><p><a href="#tab3">Jira user</a></p></li>
		<li class=""><p><a href="#tab4">Notification E-Mail</a></p></li>
		<li class=""><p><a href="#tab5">Notification E-Mail SMTP</a></p></li>
		<li class=""><p><a href="#tab6">SVN</a></p></li>
		<li class=""><p><a href="#tab7">Report template</a></p></li>
	</ul>
	<div class="custom-scroll">
		<div id="tab1" class="tabContent" style="display: none;">
			<table class="">
				<tbody>
				<tr>
					<td><label for="reportTemplateFileName">Report template file name (by default announcement.vm):&nbsp;</label></td>
					<td>
						<props:textProperty name="reportTemplateFileName" className="stringField"/>
					</td>
				</tr>
				</tbody>
			</table>

		</div>
		<div id="tab2" class="tabContent" style="display: none;">
			<table class="runnerFormTable wideLabel buildParameters">
				<tbody>
				<tr>
					<td><label for="addComment">Add comment:&nbsp;<l:star/></label></td>
					<td>
						<props:checkboxProperty name="addComment"/>
					</td>
				</tr>
				<tr>
					<td><label for="commentFormat">Comment format:&nbsp;<l:star/></label></td>
					<td class="paramValue">
						<props:textProperty name="commentFormat" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="transitionIssue">Transition issue:&nbsp;<l:star/></label></td>
					<td>
						<props:checkboxProperty name="transitionIssue"/>
					</td>
				</tr>
				<tr>
					<td><label for="transitionFormat">Transition format (FROM:TO,FROM:TO...):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="transitionFormat" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="jiraKeys">Jira project keys (example JIRA):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="jiraKeys" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="resolveVersion">Jira version (i.e. ufos-core-8.xx.0):&nbsp;</label></td>
					<td>
						<props:textProperty name="resolveVersion" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="jiraStatusIds">Issue status id's (i.e. Closed):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="jiraStatusIds" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="jiraResolutionIds">Issue resolution id's (i.e. Fixed):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="jiraResolutionIds" className="stringField"/>
					</td>
				</tr>

				</tbody>
			</table>
		</div>
		<div id="tab3" class="tabContent" style="display: none;">
			<table class="runnerFormTable wideLabel buildParameters">
				<tbody>
				<tr>
					<td><label for="jiraUrl">Jira URL:&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="jiraUrl" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="jiraUser">Jira user:&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="jiraUser" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="jiraPassword">Jira password:&nbsp;<l:star/></label></td>
					<td>
						<props:passwordProperty name="jiraPassword"/>
					</td>
				</tr>

				</tbody>
			</table>
		</div>
		<div id="tab4" class="tabContent" style="display: none;">
			<table class="runnerFormTable wideLabel buildParameters">
				<tbody>
				<%--<tr>--%>
				<%--<th><label for="reportTemplateFilePath">Report template file path (by default teamcity.build.checkoutDir + /src/main/resources/ + Report--%>
				<%--template file--%>
				<%--name):&nbsp;</label></th>--%>
				<%--<td>--%>
				<%--<props:textProperty name="reportTemplateFilePath" className="stringField"/>--%>
				<%--</td>--%>
				<%--</tr>--%>
				<tr>
					<td><label for="sendEmailNotification">Is it need to send Report via E-Mail Notification?:&nbsp;</label></td>
					<td>
						<props:checkboxProperty name="sendEmailNotification"/>
					</td>
				</tr>
				<tr>
					<td><label for="developmentTeam">Development team name report template (by default Our Development Team):&nbsp;</label></td>
					<td>
						<props:textProperty name="developmentTeam" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="emailUserName">E-mail user name:&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="emailUserName" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="emailUserPassword">Email user password:&nbsp;<l:star/></label></td>
					<td>
						<props:passwordProperty name="emailUserPassword" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="emailFrom">Email from (valid e-mail address, i.e user@user_mail.com):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="emailFrom" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="emailTo">Emails to (e-mail addresses separated by commas, i.e. user@user_mail.com):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="emailTo" className="stringField"/>
					</td>
				</tr>

				<tr>
					<td><label for="emailSubject">E-mail subject (by default UFOS-CORE RELEASE core_versionRCbuild_number:&nbsp;</label></td>
					<td>
						<props:textProperty name="emailSubject" className="stringField"/>
					</td>
				</tr>
				</tbody>
			</table>
		</div>
		<div id="tab5" class="tabContent" style="display: none;">
			<table class="runnerFormTable wideLabel buildParameters">
				<tbody>
				<tr>
					<td><label for="smtpHost">SMTP Host for recipient (i.e. smtp.user_mail.com):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="smtpHost" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="smtpPort">SMTP Port for recipient (i.e. 25):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="smtpPort" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="smtpAuth">SMTP Authentication (true or false):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="smtpAuth" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="smtpStartTls">SMTP Start TLS enable (true or false):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="smtpStartTls" className="stringField"/>
					</td>
				</tr>
				</tbody>
			</table>
		</div>
		<div id="tab6" class="tabContent" style="display: none;">
			<table class="runnerFormTable wideLabel buildParameters">
				<tbody>
				<tr>
					<td><label for="commitToSvn">Is It need to commit the Report to SVN?:&nbsp;<l:star/></label></td>
					<td>
						<props:checkboxProperty name="commitToSvn" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="svnUrl">SVN urls (i.e. https://our-svn.our-company.com/project-name/branch-folder):&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="svnUrl" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="svnUserName">SVN user name:&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="svnUserName" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="svnUserPassword">SVN user password:&nbsp;<l:star/></label></td>
					<td>
						<props:passwordProperty name="svnUserPassword" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="svnHTMLFileName">SVN HTML Report (by template) target file name:&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="svnHTMLFileName" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="svnTXTFileName">SVN TXT Report target file name:&nbsp;<l:star/></label></td>
					<td>
						<props:textProperty name="svnTXTFileName" className="stringField"/>
					</td>
				</tr>
				</tbody>
			</table>
		</div>
		<div id="tab7" class="tabContent" style="display: none;">
			<table class="runnerFormTable wideLabel buildParameters">
				<tbody>
				<tr>
					<td><label for="customJiraParameters">Map custom Jira parameters:&nbsp;</label></td>
					<td>
						<props:textProperty name="customJiraParameters" className="stringField"/>
					</td>
				</tr>
				<tr>
					<td><label for="customUserParameters">Map custom user parameters:&nbsp;</label></td>
					<td>
						<props:textProperty name="customUserParameters" className="stringField"/>
					</td>
				</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>
