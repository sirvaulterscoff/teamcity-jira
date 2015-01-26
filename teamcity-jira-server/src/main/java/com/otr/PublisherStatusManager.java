package com.otr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class PublisherStatusManager {
	private PublishStatusSettings settings;

	public PublisherStatusManager(PublishStatusSettings settings) {
		this.settings = settings;
	}

	@Nullable
	public StatusPublisher createPublisher(@NotNull Map<String, String> params) {
		return settings.createPublisher(params);
	}

	@Nullable
	public PublishStatusSettings findSettings(@NotNull String publisherId) {
		return settings;
	}

	@NotNull
	List<PublishStatusSettings> getAllPublisherSettings() {
		List<PublishStatusSettings> settings = new ArrayList<PublishStatusSettings>();
		settings.add(this.settings);
		return settings;
	}
}
