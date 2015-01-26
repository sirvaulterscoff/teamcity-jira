package com.otr;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class StatusPublisherImpl implements StatusPublisher {
	protected static final Logger log = LoggerFactory.getLogger(StatusPublisherImpl.class);

	public static final String NOT_AN_ISSUE = "not-issue";

	public static final String JIRA_PROJECT_KEYS = "jiraKeys";
	public static final String JIRA_URL = "jiraUrl";
	public static final String JIRA_USER = "jiraUser";
	public static final String JIRA_PASSWORD = "jiraPassword";
	public static final String RESOLVE_VERSION = "resolveVersion";
	public static final String COMMENT_FORMAT = "commentFormat";
	public static final String ADD_COMMENT = "addComment";

	public static final String TRANSITION_ISSUE = "transitionIssue";
	public static final String TRANSITION_FORMAT = "transitionFormat";

	private Map<String, String> params;

	public StatusPublisherImpl(Map<String, String> params) {
		this.params = params;
	}

	public void buildFinished(SRunningBuild build) {
		String jiraProjectsStr = params.get(JIRA_PROJECT_KEYS);
		Iterable<String> jiraProjects = Splitter.on(",").trimResults().omitEmptyStrings().split(jiraProjectsStr);
		boolean addComment = Boolean.parseBoolean(params.get(ADD_COMMENT));
		String commentFormat = params.get(COMMENT_FORMAT);
		String resolveVersion = params.get(RESOLVE_VERSION);
		String jiraUrl = params.get(JIRA_URL);
		String jiraUser = params.get(JIRA_USER);
		String jiraPassword = params.get(JIRA_PASSWORD);

		boolean transitionIssue = Boolean.parseBoolean(params.get(TRANSITION_ISSUE));
		String transitionFormat = params.get(TRANSITION_FORMAT);

		List<SVcsModification> changes = build.getChanges(SelectPrevBuildPolicy.SINCE_LAST_SUCCESSFULLY_FINISHED_BUILD, false);
		if (CollectionUtils.isNotEmpty(changes)) {
			Collection<String> tickets = Collections2.transform(changes, new ToTicketFunction(jiraProjects));
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
				log.debug("Processing ticket " + ticket);
				try {
					Issue issue = jira.getIssue(ticket);
					if (addComment) {
						log.debug("Adding comment to issue " + ticket);
						issue.addComment(commentFormat);
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
							log.debug(String.format("Changing state for issue %s from %s to %s", ticket, existingStatus, newStatus));
							issue.transition().execute(newStatus);
						}
					}
				} catch (JiraException e) {
					log.error("Failed to get issue " + ticket + " from jira", e);
				}
			}
		}
	}

	private class ToTicketFunction implements Function<SVcsModification, String> {
		private Iterable<String> jiraProjects;

		private ToTicketFunction(Iterable<String> jiraProjects) {
			this.jiraProjects = jiraProjects;
		}

		public String apply(SVcsModification input) {
			String commitMsg = input.getDescription();
			commitMsg = Splitter.on("\n").omitEmptyStrings().trimResults().split(commitMsg).iterator().next();
			for (String jiraProject : jiraProjects) {
				jiraProject = jiraProject.endsWith("-") ? StringUtils.removeEnd(jiraProject, "-") : jiraProject;
				Pattern pattern = Pattern.compile("(" + jiraProject + "-\\d+)[:]?\\s.*");
				final Matcher matcher = pattern.matcher(commitMsg);
				log.debug("Commit msg [" + commitMsg + "] " + (matcher.matches() ? " matches " : " skipped"));
				if (matcher.matches()) {
					String ticket = matcher.group(1);
					log.debug("Got ticket " + ticket);
					return ticket;
				}
			}
			return NOT_AN_ISSUE;
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
