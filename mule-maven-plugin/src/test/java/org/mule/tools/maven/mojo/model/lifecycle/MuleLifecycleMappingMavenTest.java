/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model.lifecycle;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_TEST_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INITIALIZE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INSTALL;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PACKAGE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_TEST_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST_COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;

import java.util.Map;
import java.util.Set;

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.junit.Test;

import com.google.common.collect.Sets;

public class MuleLifecycleMappingMavenTest {

  private final String DEFAULT_LIFECYCLE = DefaultLifecycles.STANDARD_LIFECYCLES[0];

  @Test
  public void getLifecyclePhases() {

    Set<MavenLifecyclePhase> expectedLifecyclePhases =
        Sets.newHashSet(VALIDATE, INITIALIZE, GENERATE_SOURCES, PROCESS_SOURCES, PROCESS_RESOURCES, COMPILE,
                        GENERATE_TEST_SOURCES, PROCESS_TEST_RESOURCES, TEST_COMPILE, TEST, PACKAGE, INSTALL, DEPLOY);

    MuleLifecycleMappingMaven muleLifecycleMappingMavenMock = mock(MuleLifecycleMappingMaven.class);

    doCallRealMethod().when(muleLifecycleMappingMavenMock).getLifecyclePhases();
    Map lifecyclePhases = muleLifecycleMappingMavenMock.getLifecyclePhases();


    assertThat("The number of lifecycle phases is wrong", lifecyclePhases.keySet().size(),
               is(expectedLifecyclePhases.size()));

    expectedLifecyclePhases.forEach(expectedPhase -> assertThat("Missing lifecycle phase: " + expectedPhase.id(),
                                                                lifecyclePhases.containsKey(expectedPhase.id()), is(true)));


  }

  @Test
  public void getMuleDefaultLifecycleTest() {
    MuleLifecycleMappingMaven muleLifecycleMappingMaven = new MuleLifecycleMapping().getMuleLifecycleMappingMaven();

    Map lifecycles = muleLifecycleMappingMaven.getLifecycles();
    Lifecycle defaultLifecycle = (Lifecycle) lifecycles.get(DEFAULT_LIFECYCLE);
    Map phases = defaultLifecycle.getLifecyclePhases();


    Map expectedPhases = muleLifecycleMappingMaven.getLifecyclePhases();
    for (Object phase : phases.keySet()) {
      assertThat("Current phase is not defined in the expected lifecycle map", expectedPhases.containsKey(phase), is(true));
      assertThat("Current phase is not attached to the same goals in the expected lifecycle map",
                 expectedPhases.get(phase).toString(), equalTo(phases.get(phase).toString()));
    }

    for (Object phase : expectedPhases.keySet()) {
      assertThat("Current phase is not defined in the actual lifecycle map", phases.containsKey(phase), is(true));
    }
  }

}
