package com.otr;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 23.01.2015
 */
public class PublishStatusBuildFeature extends BuildFeature {
	@NotNull
	private final PublishStatusController controller;
	@NotNull
	private final PublisherStatusManager publisherManager;

	public PublishStatusBuildFeature(@NotNull PublishStatusController controller,
	                                  @NotNull PublisherStatusManager publisherManager) {
		this.controller = controller;
		this.publisherManager = publisherManager;
	}

	@NotNull
	@Override
	public String getType() {
		return "teamcity-jira-publisher";
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "Teamcity-JIRA status publisher";
	}

	@Nullable
	@Override
	public String getEditParametersUrl() {
		return controller.getUrl();
	}

	@Override
	public boolean isMultipleFeaturesPerBuildTypeAllowed() {
		return false;
	}

	@NotNull
	@Override
	public String describeParameters(@NotNull Map<String, String> params) {
		String publisherId = params.get("publisherId");
		if (publisherId == null)
			return "";
		PublishStatusSettings settings = publisherManager.findSettings(publisherId);
		if (settings == null)
			return "";
		return settings.describeParameters(params);
	}

	@Nullable
	@Override
	public PropertiesProcessor getParametersProcessor() {
		return new PropertiesProcessor() {
			public Collection<InvalidProperty> process(Map<String, String> params) {
				List<InvalidProperty> errors = new ArrayList<InvalidProperty>();
				String voterId = params.get("publisherId");
				if (DefaultSettings.ID.equals(voterId)) {
					errors.add(new InvalidProperty("publisherId", "Select a publisher"));
					return errors;
				}
				PublishStatusSettings settings = publisherManager.findSettings(voterId);
				if (settings == null)
					return errors;
				PropertiesProcessor proc = settings.getParametersProcessor();
				if (proc != null)
					errors.addAll(proc.process(params));
				return errors;
			}
		};
	}
}
