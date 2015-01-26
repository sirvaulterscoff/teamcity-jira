package com.otr;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public interface PublishStatusSettings {

	@NotNull
	String getId();

	@NotNull
	String getName();

	@Nullable
	String getEditSettingsUrl();

	@Nullable
	Map<String, String> getDefaultParameters();

	@Nullable
	StatusPublisher createPublisher(@NotNull Map<String, String> params);

	@NotNull
	public String describeParameters(@NotNull Map<String, String> params);

	@Nullable
	public PropertiesProcessor getParametersProcessor();

}
