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
		final SBuildType buildType = build.getBuildType();
		if(buildType == null) {
			return;
		}
		getPublishers(buildType).buildFinished(build);
	}

	@Nullable
	private StatusPublisher getPublishers(@NotNull SBuildType buildType) {
		for (SBuildFeatureDescriptor buildFeatureDescriptor : buildType.getResolvedSettings().getBuildFeatures()) {
			BuildFeature buildFeature = buildFeatureDescriptor.getBuildFeature();
			if (buildFeature instanceof PublishStatusBuildFeature) {
				StatusPublisher publisher = myPublisherManager.createPublisher(buildFeatureDescriptor.getParameters());
				if (publisher != null)
					return publisher;
			}
		}
		return null;
	}
}
