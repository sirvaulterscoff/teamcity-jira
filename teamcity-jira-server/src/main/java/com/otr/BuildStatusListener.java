package com.otr;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.RunningBuildsManager;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsRootInstanceEntry;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class BuildStatusListener extends BuildServerAdapter {
	protected static final Logger log = LoggerFactory.getLogger(BuildStatusListener.class);

	private final PublisherStatusManager myPublisherManager;
	private final BuildHistory myBuildHistory;
	private final RunningBuildsManager myRunningBuilds;

	public BuildStatusListener(@NotNull EventDispatcher<BuildServerListener> events,
	                                     @NotNull PublisherStatusManager voterManager,
	                                     @NotNull BuildHistory buildHistory,
	                                     @NotNull RunningBuildsManager runningBuilds) {
		myPublisherManager = voterManager;
		myBuildHistory = buildHistory;
		myRunningBuilds = runningBuilds;
		events.addListener(this);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		try {
			buildFinished0(build);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void buildFinished0(@NotNull SRunningBuild build) {
		log.info("Check for process after build finished for build: " + build.getFullName());
		final SBuildType buildType = build.getBuildType();
		if(buildType == null) {
			return;
		}
		StatusPublisher statusPublisher = getPublishers(buildType);
		if (statusPublisher != null) {
			statusPublisher.buildFinished(build);
		}
	}

	@Nullable
	private StatusPublisher getPublishers(@NotNull SBuildType buildType) {
		log.info("Getting publishers for build type: " + buildType.getName());
		for (SBuildFeatureDescriptor buildFeatureDescriptor : buildType.getResolvedSettings().getBuildFeatures()) {
			BuildFeature buildFeature = buildFeatureDescriptor.getBuildFeature();
			if (buildFeature instanceof PublishStatusBuildFeature) {
				StatusPublisher publisher = myPublisherManager.createPublisher(buildFeatureDescriptor.getParameters());
				if (publisher != null)
					return publisher;
			}
		}
		log.info("Getting build features for build type " + buildType.getName() + " will return null");
		return null;
	}
}
