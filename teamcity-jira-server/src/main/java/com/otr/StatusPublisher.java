package com.otr;

import jetbrains.buildServer.serverSide.SRunningBuild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public interface StatusPublisher {

	void buildFinished(SRunningBuild build);
}
