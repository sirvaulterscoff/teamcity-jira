package com.otr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * TODO: comment
 * @author haritonova 17.02.2015   16:30
 */
public class SmtpAuthenticator extends Authenticator {
	String userName;
	String userPassword;

	public SmtpAuthenticator(String userName, String userPassword) {
		super();

		this.userName = userName;
		this.userPassword = userPassword;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		if ((userName != null) && (userName.length() > 0) && (userPassword != null)
		    && (userPassword.length() > 0)) {

			return new PasswordAuthentication(userName, userPassword);
		}

		return null;
	}
}
