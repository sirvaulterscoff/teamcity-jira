package com.otr;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class PublishStatusSettingsController extends BaseController {

	private final String url;
	private final PublisherStatusManager myPublisherManager;

	public PublishStatusSettingsController(@NotNull WebControllerManager controllerManager,
	                                       @NotNull PluginDescriptor descriptor,
	                                       @NotNull PublisherStatusManager publisherManager) {
		url = descriptor.getPluginResourcesPath("publishStatusSettings.html");
		myPublisherManager = publisherManager;
		controllerManager.registerController(url, this);
	}

	public String getUrl() {
		return url;
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
		String publisherId = request.getParameter("publisherId");
		if (publisherId == null) {
			return null;
		}
		request.setAttribute("projectId", request.getParameter("projectId"));
		PublishStatusSettings settings = myPublisherManager.findSettings(publisherId);
		if (settings == null) {
			return null;
		}
		String settingsUrl = settings.getEditSettingsUrl();
		Map<String, String> params = settings.getDefaultParameters() != null ? settings.getDefaultParameters() : Collections.<String, String>emptyMap();
		request.setAttribute("propertiesBean", new BasePropertiesBean(params));
		if (settingsUrl != null) {
			request.getRequestDispatcher(settings.getEditSettingsUrl()).include(request, response);
		}
		return null;
	}
}
