package com.otr;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class DefaultSettings implements PublishStatusSettings{
	public static final String ID = "--";
	private final PluginDescriptor myDescriptor;
	private final ExtensionHolder myExtensionHolder;
	private final WebLinks myLinks;

	public DefaultSettings(@NotNull PluginDescriptor descriptor,
	                      @NotNull ExtensionHolder extensionHolder,
	                      @NotNull WebLinks links) {
		myDescriptor = descriptor;
		myExtensionHolder = extensionHolder;
		myLinks = links;
	}

	@NotNull
	public String getId() {
		return ID;
	}

	@NotNull
	public String getName() {
		return "Settings";
	}

	@Nullable
	public String getEditSettingsUrl() {
		return myDescriptor.getPluginResourcesPath("statusPublisher.jsp");
	}

	@Nullable
	public StatusPublisher createPublisher(@NotNull Map<String, String> params) {
		return new StatusPublisherImpl(params);
	}

	@Nullable
	public Map<String, String> getDefaultParameters() {
		return null;
	}

	@NotNull
	public String describeParameters(@NotNull Map<String, String> params) {
		return "";
	}

	@Nullable
	public PropertiesProcessor getParametersProcessor() {
		return null;
	}
}
