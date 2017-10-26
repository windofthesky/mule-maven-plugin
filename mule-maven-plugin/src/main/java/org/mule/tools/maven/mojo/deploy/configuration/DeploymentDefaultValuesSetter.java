/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.MuleRuntimeDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

import java.io.File;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DeploymentDefaultValuesSetter {

  public static void setDefaultValues(Deployment deployment, MavenProject project) {
    if (deployment instanceof AgentDeployment) {
      setAgentDeploymentDefaultValues((AgentDeployment) deployment, project);
    } else if (deployment instanceof StandaloneDeployment) {
      setStandaloneDeploymentDefaultValues((StandaloneDeployment) deployment, project);
    } else if (deployment instanceof ClusterDeployment) {
      setClusterDeploymentDefaultValues((ClusterDeployment) deployment, project);
    } else if (deployment instanceof CloudHubDeployment) {
      setCloudHubDeploymentDefaultValues((CloudHubDeployment) deployment, project);
    } else if (deployment instanceof ArmDeployment) {
      setArmDeploymentDefaultValues((ArmDeployment) deployment, project);
    }
  }


  private static void setDeploymentValues(Deployment deployment, MavenProject project) {
    if (isBlank(deployment.getApplicationName())) {
      deployment.setApplicationName(getProperty("applicationName", project.getArtifactId()));
    }

    if (isBlank(deployment.getSkip())) {
      deployment.setApplicationName(getProperty("mule.skip", "false"));
    }

    if (!deployment.getMuleVersion().isPresent()) {
      deployment.setApplicationName(getProperty("mule.version"));
    }

    if (deployment.getApplication() == null) {
      deployment.setApplication(new File(getProperty("mule.application",
                                                     project.getAttachedArtifacts().get(0).getFile().getAbsolutePath())));
    }
  }

  private static void setMuleRuntimeDeploymentValues(MuleRuntimeDeployment deployment, MavenProject project) {
    setDeploymentValues(deployment, project);

    if (deployment.getScript() == null) {
      String scriptCandidate = getProperty("script");
      if (isNotBlank(scriptCandidate)) {
        deployment.setScript(new File(scriptCandidate));
      }
    }

    if (deployment.getTimeout() == null) {
      String timeoutCandidate = getProperty("mule.timeout");
      if (isNotBlank(timeoutCandidate)) {
        deployment.setTimeout(Integer.valueOf(timeoutCandidate));
      }
    }

    if (deployment.getDeploymentTimeout() == null) {
      deployment.setDeploymentTimeout(Long.valueOf(getProperty("mule.deploymentConfiguration.timeout", "60000")));
    }

    if (deployment.getArguments() == null) {
      String argumentsCandidate = getProperty("mule.arguments");
      if (isNotBlank(argumentsCandidate)) {
        deployment.setArguments(argumentsCandidate.split(","));
      }
    }

    if (deployment.getMuleHome() == null) {
      deployment.setMuleHome(new File(getProperty("mule.home")));
    }
  }

  private static void setAnypointDeploymentValues(AnypointDeployment deployment, MavenProject project) {
    setDeploymentValues(deployment, project);
    if (isBlank(deployment.getUri())) {
      deployment.setUri(getProperty("anypoint.uri", "https://anypoint.mulesoft.com"));
    }

    if (isBlank(deployment.getBusinessGroup())) {
      deployment.setBusinessGroup(getProperty("anypoint.businessGroup", StringUtils.EMPTY));
    }

    if (isBlank(deployment.getEnvironment())) {
      deployment.setEnvironment(getProperty("anypoint.environment"));
    }

    if (isBlank(deployment.getPassword())) {
      deployment.setPassword(getProperty("anypoint.password"));
    }

    if (isBlank(deployment.getServer())) {
      deployment.setServer(getProperty("maven.server"));
    }

    if (isBlank(deployment.getUsername())) {
      deployment.setUsername(getProperty("anypoint.username"));
    }
  }


  public static void setAgentDeploymentDefaultValues(AgentDeployment deployment, MavenProject project) {
    setDeploymentValues(deployment, project);

    if (isBlank(deployment.getUri())) {
      deployment.setApplicationName(getProperty("anypoint.uri", "https://anypoint.mulesoft.com"));
    }
  }

  public static void setStandaloneDeploymentDefaultValues(StandaloneDeployment deployment, MavenProject project) {
    setMuleRuntimeDeploymentValues(deployment, project);
  }

  public static void setClusterDeploymentDefaultValues(ClusterDeployment deployment, MavenProject project) {
    setMuleRuntimeDeploymentValues(deployment, project);

    if (deployment.getSize() == null) {
      deployment.setSize(2);
    }
  }

  public static void setArmDeploymentDefaultValues(ArmDeployment deployment, MavenProject project) {
    setAnypointDeploymentValues(deployment, project);

    if (!deployment.isArmInsecure().isPresent()) {
      deployment.setArmInsecure(Boolean.valueOf(getProperty("arm.insecure", "false")));
    }

    if (!deployment.isFailIfNotExists().isPresent()) {
      deployment.setFailIfNotExists(Boolean.TRUE);
    }

    if (isBlank(deployment.getTarget())) {
      deployment.setTarget(getProperty("anypoint.target"));
    }

    if (deployment.getTargetType() == null) {
      deployment.setTarget(getProperty("anypoint.target.type"));
    }
  }

  public static void setCloudHubDeploymentDefaultValues(CloudHubDeployment deployment, MavenProject project) {
    setAnypointDeploymentValues(deployment, project);

    if (!deployment.getWorkers().isPresent()) {
      deployment.setWorkers(Integer.valueOf(getProperty("cloudhub.workers", "1")));
    }

    if (isBlank(deployment.getWorkerType())) {
      deployment.setWorkerType(getProperty("cloudhub.workerType", "Medium"));
    }

    if (isBlank(deployment.getRegion())) {
      deployment.setRegion(getProperty("cloudhub.region", "us-east-1"));
    }
  }
}
