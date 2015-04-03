package com.otr;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.ICredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import net.rcarz.jiraclient.Status;
import net.rcarz.jiraclient.Version;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class StatusPublisherImpl implements StatusPublisher {
	protected static final Logger log = LoggerFactory.getLogger(StatusPublisherImpl.class);
	public static final String PLACEHOLDER_PREFIX = "#{";
	public static final String PLACEHOLDER_SUFFIX = "}";
	public static final String SPLITTER = ",";
	public static final String PAIR_SPLITTER = ":";
	public static final String VALUE_SPLITTER = "\\|";
	public static final String NULL_MESSAGE_REPLACEMENT = "-";

	public static final String NOT_AN_ISSUE = "not-issue";

	public static final String JIRA_STATUS_IDS = "jiraStatusIds";
	public static final String JIRA_RESOLUTION_IDS = "jiraResolutionIds";
	public static final String JIRA_DEFAULT_STATUS_IDS = "Closed";
	public static final String JIRA_DEFAULT_RESOLUTION_IDS = "Fixed";
	public static final String JIRA_FILTER_FIELDS = "jiraFilterFields";
	public static final String JIRA_FILTER_FIELDS_AND = "jiraFilterFieldsAnd";

	public static final String JIRA_PROJECT_KEYS = "jiraKeys";
	public static final String JIRA_URL = "jiraUrl";
	public static final String JIRA_USER = "jiraUser";
	public static final String JIRA_PASSWORD = "jiraPassword";
	public static final String RESOLVE_VERSION = "resolveVersion";
	public static final String COMMENT_FORMAT = "commentFormat";
	public static final String ADD_COMMENT = "addComment";

	public static final String TRANSITION_ISSUE = "transitionIssue";
	public static final String TRANSITION_FORMAT = "transitionFormat";

	public static final String SEND_EMAIL_NOTIFICATION = "sendEmailNotification";
	public static final String EMAIL_USER_NAME = "emailUserName";
	public static final String EMAIL_USER_PASSWORD = "emailUserPassword";
	public static final String EMAIL_FROM = "emailFrom";
	public static final String EMAIL_TO = "emailTo";
	public static final String EMAIL_SMTP_HOST = "smtpHost";
	public static final String EMAIL_SMTP_PORT = "smtpPort";
	public static final String EMAIL_SMTP_AUTH = "smtpAuth";
	public static final String EMAIL_SMTP_STARTTLS = "smtpStartTls";

	public static final String COMMIT_TO_SVN = "commitToSvn";
	public static final String SVN_URL = "svnUrl";
	public static final String SVN_USER_NAME = "svnUserName";
	public static final String SVN_USER_PASSWORD = "svnUserPassword";
	public static final String SVN_HTMLFILE_NAME = "svnHTMLFileName";
	public static final String SVN_TXTFILE_NAME = "svnTXTFileName";

	public static final String EMAIL_SUBJECT = "emailSubject";
	public static final String EMAIL_DEFAULT_SUBJECT = "Announcement Notification";

	public static final String REPORTTXT_DEFAULT_SUBJECT = "The report about the next changes:";
	public static final String REPORTTXT_COMMIT_SUBJECT = "reportTxtCommitSubject";
	public static final String REPORTTXT_COMMIT_EMPTY_ISSUELIST = "reportTxtCommitEmptyIssueList";

	//	public static final String REPORT_FILE_PATH = "reportFilePath";
//	public static final String REPORT_FILE_NAME = "reportFileName";
//	public static final String REPORT_TEMPLATE_FILE_PATH = "reportTemplateFilePath";
	public static final String REPORT_TEMPLATE_FILE_NAME = "reportTemplateFileName";

	public static final String DEVELOPMENT_TEAM_NAME = "developmentTeam";
	public static final String DEFAULT_DEVELOPMENT_TEAM_NAME = "Our Development Team";

	public static final String BUILD_TRIGGERED_BY = "triggeredBy";
	public static final String CVS_REVISION_NUMBER = "cvsRevisionNumber";

	public static final String ISSUE_LIST = "issueList";

	public static final String ISSUE_KEY = "issueKey";
	public static final String ISSUE_SUMMARY = "issueSummary";
	public static final String ISSUE_TYPE = "issueType";
	public static final String ISSUE_PRIORITY = "issuePriority";
	public static final String ISSUE_AUTOR = "issueAutor";

	public static final String CUSTOM_JIRA_PARAMETERS = "customJiraParameters";
	public static final String CUSTOM_USER_PARAMETERS = "customUserParameters";

	private Map<String, String> params;

	public StatusPublisherImpl(Map<String, String> params) {
		this.params = params;
	}

	public void buildFinished(SRunningBuild build) {
		log.info("Teamcity-jira plugin starts process after build finished");
		jetbrains.buildServer.messages.Status buildState = build.getBuildStatus();
		if (!buildState.equals(jetbrains.buildServer.messages.Status.NORMAL)) {
			log.info("The build was finish not with the NORMAL State, ut with the State [" + buildState + "]");
			return;
		}
		ParametersProvider parametersProvider = build.getParametersProvider();
		Map<String, String> parametersProviderAll = parametersProvider.getAll();

		Map<String, String> customJiraParameters = buildParametersMap(params.get(CUSTOM_JIRA_PARAMETERS), parametersProviderAll);
		String jiraProjectsStr = formatComment(params.get(JIRA_PROJECT_KEYS), parametersProviderAll);
		Iterable<String> jiraProjects = Splitter.on(SPLITTER).trimResults().omitEmptyStrings().split(jiraProjectsStr);

		boolean addComment = Boolean.parseBoolean(params.get(ADD_COMMENT));
		String commentFormat = params.get(COMMENT_FORMAT);

		Map<String, String> jiraFilterFieldsList = buildParametersMap(params.get(JIRA_FILTER_FIELDS), parametersProviderAll);
		boolean jiraFilterFieldsAnd = Boolean.parseBoolean(params.get(JIRA_FILTER_FIELDS_AND));

		String jiraStatusIdsList = formatComment(params.get(JIRA_STATUS_IDS), parametersProviderAll);
		String[] jiraStatusIds;
		if (jiraStatusIdsList != null) {
			jiraStatusIds = jiraStatusIdsList.trim().split(SPLITTER);
		} else {
			jiraStatusIds = new String[1];
			jiraStatusIds[0] = JIRA_DEFAULT_STATUS_IDS;
		}
		String jiraResolutionIdsList = formatComment(params.get(JIRA_RESOLUTION_IDS), parametersProviderAll);
		String[] jiraResolutionIds;
		if (jiraResolutionIdsList != null) {
			jiraResolutionIds = jiraResolutionIdsList.trim().split(SPLITTER);
		} else {
			jiraResolutionIds = new String[1];
			jiraResolutionIds[0] = JIRA_DEFAULT_RESOLUTION_IDS;
		}
		String resolveVersion = formatComment(params.get(RESOLVE_VERSION), parametersProviderAll);
		String jiraUrl = formatComment(params.get(JIRA_URL), parametersProviderAll);
		String jiraUser = formatComment(params.get(JIRA_USER), parametersProviderAll);
		String jiraPassword = formatComment(params.get(JIRA_PASSWORD), parametersProviderAll);

		boolean transitionIssue = Boolean.parseBoolean(params.get(TRANSITION_ISSUE));
		String transitionFormat = formatComment(params.get(TRANSITION_FORMAT), parametersProviderAll);

		List<SVcsModification> changes = build.getChanges(SelectPrevBuildPolicy.SINCE_LAST_SUCCESSFULLY_FINISHED_BUILD, true);
		List<Map<String, String>> reportedTicketsList = new ArrayList();
		log.info("Check build changes as not empty");
		if (CollectionUtils.isNotEmpty(changes)) {
			log.info("Changes is not empty, will process changes");
			Collection<Set<String>> ticketGroups = Collections2.transform(changes, new ToTicketFunction(jiraProjects));
			Set<String> tickets = new HashSet<String>();
			for (Set<String> ticketGroup : ticketGroups) {
				tickets.addAll(ticketGroup);
			}
			BasicCredentials creds = new BasicCredentials(jiraUser, jiraPassword);
			JiraClient jira = new NonVerifingJiraClient(jiraUrl, creds);
			try {
				((NonVerifingJiraClient) jira).hack();
			} catch (Exception e) {
				log.error("Failed to hack JIRA client", e);
				jira = new JiraClient(jiraUrl, creds);
			}
			for (String ticket : tickets) {
				if (NOT_AN_ISSUE.equals(ticket)) {
					continue;
				}
				log.info("Processing ticket " + ticket);
				try {
					Issue issue = jira.getIssue(ticket);
					// filter by status and resolution
					Status status = issue.getStatus();
					boolean isValidIssue = false;
					boolean isChecked = checkJiraCustomFilterFields(jiraFilterFieldsList, issue);
					if (status == null && issue.getResolution() == null) {
						log.info("There is the changes with the status and the resolution are equals null. The information about these issues will not added to the report and will not be transited to the next Status.");
					} else if (!Arrays.asList(jiraStatusIds).contains(status.getName()) && (issue.getResolution() == null || !Arrays.asList(jiraResolutionIds).contains(issue.getResolution().getName()))) {
						log.info("There is the changes with the status and the resolution not in the user set found.");
						if (!isChecked || (isChecked && jiraFilterFieldsAnd)) {
							log.info("There is the changes with the status and the resolution not in the user set found. The information about these issues will not added to the report and will not be transited to the next Status:");
							log.info("Issue: ", issue.getKey());
							isValidIssue = false;
						} else {
							isValidIssue = true;
						}
					} else if (isChecked || (!isChecked && !jiraFilterFieldsAnd)) {
						isValidIssue = true;
					}
					if (isValidIssue) {
//						if ((checkJiraCustomFilterFields(jiraFilterFieldsList, issue) && jiraFilterFieldsAnd) || (!checkJiraCustomFilterFields(jiraFilterFieldsList, issue) && !jiraFilterFieldsAnd))
//					if (((Arrays.asList(jiraStatusIds).contains(status.getName()) && (issue.getResolution() == null || Arrays.asList(jiraResolutionIds).contains(issue.getResolution().getName()))) || (!jiraFilterFieldsAnd)) &&
//					    (checkJiraCustomFilterFields(jiraFilterFieldsList, issue))) {

						log.info("The current ticket [" + ticket + "] will be added to the report list.");

						HashMap<String, String> reportedTicketInfo = new HashMap<String, String>();
						reportedTicketInfo.put(ISSUE_KEY, issue.getKey());
						reportedTicketInfo.put(ISSUE_SUMMARY, issue.getSummary());
						reportedTicketInfo.put(ISSUE_PRIORITY, issue.getPriority().getName());
						reportedTicketInfo.put(ISSUE_TYPE, issue.getIssueType().getName());
						reportedTicketInfo.put(ISSUE_AUTOR, issue.getReporter().getDisplayName());
						if (customJiraParameters != null) {
							Object field = null;
							for (String key : customJiraParameters.keySet()) {
								field = issue.getField(key);
								if (field != null) {
									if (field instanceof JSONObject) {
										field = ((JSONObject) field).getString("value");
									}
									reportedTicketInfo.put(customJiraParameters.get(key), field.toString());
								} else {
									reportedTicketInfo.put(customJiraParameters.get(key), NULL_MESSAGE_REPLACEMENT);
								}
							}
						}
						reportedTicketsList.add(reportedTicketInfo);

						if (addComment) {
							log.info("Adding comment to issue " + ticket);
							issue.addComment(formatComment(commentFormat, build.getParametersProvider().getAll()));
						}
						if (!Iterables
								.tryFind(issue.getFixVersions(), new FindVersionPredicate(RESOLVE_VERSION))
								.isPresent()) {
							List versions = new LinkedList(issue.getFixVersions());
							versions.add(resolveVersion);
							issue.update().field(Field.FIX_VERSIONS, versions).execute();
						}

						if (transitionIssue) {
							Map<String, String> transitionMap = Splitter.on(",").trimResults().omitEmptyStrings().withKeyValueSeparator(":").split(transitionFormat);
							Status existingStatus = issue.getStatus();
							String newStatus = transitionMap.get(existingStatus.getName());
							if (StringUtils.isNotBlank(newStatus)) {
								log.info(String.format("Changing state for issue %s from %s to %s", ticket, existingStatus, newStatus));
								issue.transition().execute(newStatus);
							}
						}
					}
				} catch (JiraException e) {
					if ((e).getCause() instanceof UnknownHostException) {
						log.error("Unknown Jira host or Jira is in inaccessible state. The Jira report will not created. ", e.getCause());
						return;
					}
					log.error("Failed to get issue " + ticket + " from jira", e);
				}
			}
		}

		log.info("Start to create template.");

		Properties properties = System.getProperties();

//		String userFilePath = params.get(REPORT_TEMPLATE_FILE_PATH);
		String userFileName = formatComment(params.get(REPORT_TEMPLATE_FILE_NAME), parametersProviderAll);
		String templatePath = "";
		String templateName = "";
//		if (userFilePath == null || userFilePath.isEmpty()) {
		templatePath = build.getParametersProvider().get("teamcity.build.checkoutDir") + "/src/main/resources/announcements/";
//		} else {
//			templatePath = build.getParametersProvider().get("teamcity.build.checkoutDir") + userFilePath;
//		}
		if (userFileName == null || userFileName.isEmpty()) {
			templateName = "announcement.vm";
		} else {
			templateName = userFileName;
		}

		long srcRevision = -1;

		Map<String, String> customUserParameters = buildParametersMap(params.get(CUSTOM_USER_PARAMETERS), parametersProviderAll);

		Map<String, String> customParameters = new HashMap<String, String>(customUserParameters);

		if (Boolean.parseBoolean(params.get(COMMIT_TO_SVN))) {
			srcRevision = addToSVN(params, processHTML(templatePath, templateName, reportedTicketsList, customParameters, build, -1), formatComment(params.get(SVN_HTMLFILE_NAME), parametersProviderAll));

			String txtSubject = formatComment(params.get(REPORTTXT_COMMIT_SUBJECT), parametersProviderAll);
			String txtEmptyIssueMessage = formatComment(params.get(REPORTTXT_COMMIT_EMPTY_ISSUELIST), parametersProviderAll);
			addToSVN(params, processTXT(reportedTicketsList, txtSubject, txtEmptyIssueMessage), formatComment(params.get(SVN_TXTFILE_NAME), parametersProviderAll));
		}

		if (Boolean.parseBoolean(params.get(SEND_EMAIL_NOTIFICATION))) {
			sendEmails(properties, params, processHTML(templatePath, templateName, reportedTicketsList, customParameters, build, srcRevision), "text/html; charset=UTF-8", build.getParametersProvider());
		}
	}

	private static boolean checkJiraCustomFilterFields(Map<String, String> jiraFilterFieldsList, Issue issue) {
		if (jiraFilterFieldsList == null || jiraFilterFieldsList.isEmpty()) {
			return true;
		}
		for (String jiraFieldName : jiraFilterFieldsList.keySet()) {
			String[] jiraFieldValues = jiraFilterFieldsList.get(jiraFieldName).trim().split(VALUE_SPLITTER);

			Object field = issue.getField(jiraFieldName);
			if (field instanceof JSONObject) {
				field = ((JSONObject) field).getString("value");
			}
			if (Arrays.asList(jiraFieldValues).contains(field.toString())) {
				return true;
			}
		}
		return false;
	}

	private static Map<String, String> buildParametersMap(String sourceParameters, Map<String, String> tcParameters) {
		Map<String, String> parameters = new HashMap<String, String>();
		if (sourceParameters == null) {
			return parameters;
		}
		Iterable<String> parameterPairs = Splitter.on(SPLITTER).trimResults().omitEmptyStrings().split(sourceParameters);
		for (String parameterPair : parameterPairs) {
			Iterable<String> parameter = Splitter.on(PAIR_SPLITTER).trimResults().omitEmptyStrings().split(parameterPair);
			Iterator<String> parameterIterator = parameter.iterator();
			try {
				parameters.put(parameterIterator.next(), formatComment(parameterIterator.next(), tcParameters));
			} catch (NoSuchElementException e) {
				log.info("Wrong parameters definition. Parameters map should be like follow: source_parameter_1:target_parameter_1,source_parameter_2:target_parameter_2");
			}

		}
		return parameters;
	}

	private static String processTXT(List<Map<String, String>> reportedTicketList, String reportSubject, String emptyIssueListMessage) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(reportSubject);
		stringBuilder.append("\r\n");
		if (reportedTicketList == null || reportedTicketList.isEmpty()) {
			stringBuilder.append(emptyIssueListMessage);
			stringBuilder.append("\r\n");
			stringBuilder.append("\r\n");

			return stringBuilder.toString();
		}
		for (Map<String, String> ticketInfo : reportedTicketList) {
			stringBuilder.append(ticketInfo.get(ISSUE_KEY));
			stringBuilder.append(": ");
			stringBuilder.append(ticketInfo.get(ISSUE_SUMMARY));
			stringBuilder.append("\r\n");
		}
		stringBuilder.append("\r\n");

		return stringBuilder.toString();
	}

	private static long addToSVN(Map<String, String> params, String reportContent, String revisionFilePath) {
		long srcRevision = -1;
		SVNCommitInfo svnCommitInfo = null;

		try {
			log.info("Start to init SVN connection.");

			DAVRepositoryFactory.setup();

			String name = params.get(SVN_USER_NAME);
			String password = params.get(SVN_USER_PASSWORD);

			Iterable<String> urls = Splitter.on(SPLITTER).trimResults().omitEmptyStrings().split(params.get(SVN_URL));

			for (String url : urls) {
				SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));

				ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);

				repository.setAuthenticationManager(authManager);

				SVNNodeKind nodeKind = repository.checkPath(revisionFilePath, -1);

				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream revisionFileServerContent = new ByteArrayOutputStream();

				ISVNEditor editor = null;
				try {
					log.info("Start to create template and commit it to SVN.");

					ByteArrayOutputStream mergedFileContent = new ByteArrayOutputStream();
					mergedFileContent.write(reportContent.getBytes());

					if (nodeKind == SVNNodeKind.NONE) {
						log.info("There is no entry [ " + revisionFilePath + " ] at '" + url + "'.");
						log.info("Will be added and commited the new one after report will created.");
						editor = repository.getCommitEditor("file contents changed", null);
						svnCommitInfo = addDir(editor, revisionFilePath, reportContent.getBytes());
						log.info("The file was added: " + svnCommitInfo);
						// info updates because is the current revision should be
						srcRevision = repository.getLatestRevision();
					} else if (nodeKind == SVNNodeKind.DIR) {
						log.info("The entry [ " + revisionFilePath + " ] at '" + url + "' is a directory while a file was expected.");
					} else if (nodeKind == SVNNodeKind.FILE) {
						repository.getFile(revisionFilePath, -1, fileProperties, revisionFileServerContent);
						mergedFileContent.write(revisionFileServerContent.toByteArray());
						editor = repository.getCommitEditor("file contents changed", null);
						svnCommitInfo = modifyFile(editor, revisionFilePath, revisionFileServerContent.toByteArray(), mergedFileContent.toByteArray());
						log.info("The file was changed: " + svnCommitInfo);
						// after updates because is the current revision should be
						srcRevision = repository.getLatestRevision();
					}
				} catch (SVNException svne) {
					if (editor != null) {
						editor.abortEdit();
					}
					log.error("The error was occurs when tried to commit Jira report file to SVN ", svne);
					return -1;
				}
			}
		} catch (Exception e) {
			log.error("The error was occurs when tried to update Jira report file at SVN", e);
			return -1;
		}
		return srcRevision;
	}

	private static void sendEmails(Properties properties, Map<String, String> params, String reportContent, String typeContent, ParametersProvider parametersProvider) {
		try {
			Map<String, String> parametersProviderAll = parametersProvider.getAll();
			String emailUserName = formatComment(params.get(EMAIL_USER_NAME), parametersProviderAll);
			String emailUserPassword = formatComment(params.get(EMAIL_USER_PASSWORD), parametersProviderAll);
			String emailFrom = formatComment(params.get(EMAIL_FROM), parametersProviderAll);
			//there cans be more than one e-mail
			String[] emailsTo = Iterables.toArray(Splitter.on(SPLITTER).trimResults().omitEmptyStrings().split(formatComment(params.get(EMAIL_TO), parametersProviderAll)), String.class);
			String smtpHost = formatComment(params.get(EMAIL_SMTP_HOST), parametersProviderAll);
			String smtpPort = formatComment(params.get(EMAIL_SMTP_PORT), parametersProviderAll);
			String smtpAuth = formatComment(params.get(EMAIL_SMTP_AUTH), parametersProviderAll);
			String smtpStartTls = formatComment(params.get(EMAIL_SMTP_STARTTLS), parametersProviderAll);

			properties.setProperty("mail.smtp.host", smtpHost);
			properties.setProperty("mail.smtp.port", smtpPort);
			properties.setProperty("mail.smtp.auth", smtpAuth);
			properties.setProperty("mail.smtp.starttls.enable", smtpStartTls);

			log.info("Start to create template and send it by e-mail.");

			SmtpAuthenticator authentication = new SmtpAuthenticator(emailUserName, emailUserPassword);
			Session session = Session.getInstance(properties, authentication);

			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(emailFrom));
			log.info("The Notification e-mail From set.");

			InternetAddress[] internetAdresses = new InternetAddress[emailsTo.length];
			for (int i = 0; i < internetAdresses.length; i++) {
				internetAdresses[i] = new InternetAddress(emailsTo[i]);
			}
			message.addRecipients(Message.RecipientType.TO,
					internetAdresses);
			log.info("The Notification e-mail To set.");

			String subject = params.get(EMAIL_SUBJECT);
			if (subject == null || subject.isEmpty()) {
				subject = EMAIL_DEFAULT_SUBJECT;
			}

			message.setSubject(formatComment(subject, parametersProviderAll));
			log.info("The Notification e-mail subject set.");

			message.setContent(reportContent, typeContent);
			log.info("The Notification e-mail content set.");

			Transport.send(message);
			log.info("Sent e-mail message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	private static SVNCommitInfo addDir(ISVNEditor editor, String filePath, byte[] data) throws SVNException {
		log.info("The SVN add Dir|File started.");

		editor.openRoot(-1);
		log.info("The SVN root opened.");

		editor.addFile(filePath, null, -1);
		log.info("The SVN file added [" + filePath + "]");

		editor.applyTextDelta(filePath, null);
		log.info("The SVN text delta applied for [" + filePath + "]");

		SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
		String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(data), editor, true);
		log.info("The SVN text delta sent for [" + filePath + "]");

		editor.closeFile(filePath, checksum);
		log.info("The SVN Editor closed the file");

		editor.closeDir();
		log.info("The SVN Editor closed the directory");

		return editor.closeEdit();
	}

	private static SVNCommitInfo modifyFile(ISVNEditor editor, String filePath, byte[] oldData, byte[] newData) throws SVNException {
		log.info("The SVN modify Dir|File started.");

		editor.openRoot(-1);
		log.info("The SVN root opened.");

		editor.openFile(filePath, -1);
		log.info("The SVN file opened [" + filePath + "]");

		editor.applyTextDelta(filePath, null);
		log.info("The SVN text delta applied for [" + filePath + "]");

		SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
		String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);
		log.info("The SVN text delta sent for [" + filePath + "]");

		editor.closeFile(filePath, checksum);
		log.info("The SVN Editor closed the file");

		editor.closeDir();
		log.info("The SVN Editor closed the directory");

		return editor.closeEdit();
	}

	//templatePath == build.getParametersProvider().get("teamcity.build.checkoutDir") + params.get(REPORT_TEMPLATE_FILE_PATH) + System.getProperty("file.separator")
	public String processHTML(String templatePath, String templateName, List<Map<String, String>> reportedTicketsList, Map<String, String> customParameters, SRunningBuild build, long srcRevision) {
		log.info("The process template report started.");

		ParametersProvider parametersProvider = build.getParametersProvider();

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("file.resource.loader.path", templatePath);
		ve.init();
		Template t = ve.getTemplate(templateName, "UTF-8");

		log.info("The template report blank created.");

		VelocityContext context = new VelocityContext();

		log.info("The template report parameters start to set...");

		String developmentTeamName = parametersProvider.get("development.team.name");
		if (developmentTeamName == null || developmentTeamName.isEmpty()) {
			developmentTeamName = DEFAULT_DEVELOPMENT_TEAM_NAME;
		}
		context.put(DEVELOPMENT_TEAM_NAME, developmentTeamName);
		context.put(BUILD_TRIGGERED_BY, build.getTriggeredBy().getAsString());
		String revisionNumber = "";
		if (srcRevision > 0) {
			revisionNumber = "?p=" + srcRevision;
		}
		context.put(CVS_REVISION_NUMBER, revisionNumber);

		context.put(ISSUE_LIST, reportedTicketsList);

		if (customParameters != null) {
			for (String customKey : customParameters.keySet()) {
				String value = customParameters.get(customKey);
				if (value == null || "null".equalsIgnoreCase(value)) {
					context.put(customKey, NULL_MESSAGE_REPLACEMENT);
				} else {
					context.put(customKey, value);
				}
			}
		}

		log.info("The template report parameters was set successfully.");

		StringWriter writer = new StringWriter();

		log.info("The template report will start to merge the context with the parameters.");

		t.merge(context, writer);

		log.info("The template report was merged and created successfully.");

		return writer.toString();
	}

	private static String formatComment(String commentFormat, Map<String, String> args) {
		String out = commentFormat;
		if (commentFormat == null)
			return out;
		for (String arg : args.keySet()) {
			out = Pattern.compile(Pattern.quote(PLACEHOLDER_PREFIX + arg + PLACEHOLDER_SUFFIX)).
					matcher(out).
					replaceAll(args.get(arg));
		}
		return out;
	}

	private class ToTicketFunction implements Function<SVcsModification, Set<String>> {
		private Iterable<String> jiraProjects;

		private ToTicketFunction(Iterable<String> jiraProjects) {
			this.jiraProjects = jiraProjects;
		}

		public Set<String> apply(SVcsModification input) {
			String commitMsg = input.getDescription();
			Iterable<String> commitMsgs = Splitter.on("\n").omitEmptyStrings().trimResults().split(commitMsg);//.iterator().next();
			Set<String> result = new HashSet<String>();
			for (String msg : commitMsgs) {
				for (String jiraProject : jiraProjects) {
					jiraProject = jiraProject.endsWith("-") ? StringUtils.removeEnd(jiraProject, "-") : jiraProject;
					Pattern pattern = Pattern.compile("^(" + jiraProject + "-\\d+)[:]?(\\s|$).*");
					final Matcher matcher = pattern.matcher(msg);
					log.info("Commit msg [" + msg + "] " + (matcher.matches() ? " matches " : " skipped"));
					if (matcher.matches()) {
						String ticket = matcher.group(1);
						result.add(ticket);
						log.info("Got ticket " + ticket);
					}
				}
			}
			if (result.isEmpty()) {
				result.add(NOT_AN_ISSUE);
			}
			return result;
		}
	}

	private static class NonVerifingJiraClient extends JiraClient {
		private String uri;
		private ICredentials creds;

		public NonVerifingJiraClient(String uri) {
			super(uri);
			this.uri = uri;
		}

		public NonVerifingJiraClient(String uri, ICredentials creds) {
			super(uri, creds);
			this.uri = uri;
			this.creds = creds;
		}

		public void hack() throws NoSuchAlgorithmException, KeyManagementException, NoSuchFieldException, IllegalAccessException {
			SSLContext ctx = SSLContext.getInstance("SSL");
			X509TrustManager tm = new X509TrustManager() {

				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[]{tm}, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = new BasicClientConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", 443, ssf));

			DefaultHttpClient client = new DefaultHttpClient(ccm);
			java.lang.reflect.Field clientField = getClass().getSuperclass().getDeclaredField("restclient");
			clientField.setAccessible(true);
			clientField.set(this, new RestClient(client, creds, URI.create(uri)));
		}
	}

	private class FindVersionPredicate implements Predicate<Version> {
		private String resolveVersion;

		public FindVersionPredicate(String resolveVersion) {
			this.resolveVersion = resolveVersion;
		}

		public boolean apply(@Nullable Version input) {
			return input != null && resolveVersion.equals(input.getName());
		}
	}
}
