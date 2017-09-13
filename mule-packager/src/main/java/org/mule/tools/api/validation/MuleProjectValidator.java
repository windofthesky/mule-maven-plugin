/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

/**
 * Ensures the project is valid
 */
public class MuleProjectValidator extends AbstractProjectValidator {


  private static final int MULE_PROJECT_MAXIMUM_NUMBER_OF_DOMAINS = 1;
  private final List<SharedLibraryDependency> sharedLibraries;

  public MuleProjectValidator(Path projectBaseDir, String packagingType,
                              Project dependencyProject, MulePluginResolver resolver,
                              List<SharedLibraryDependency> sharedLibraries) {
    super(projectBaseDir, packagingType, dependencyProject, resolver);
    this.sharedLibraries = sharedLibraries;
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    isProjectStructureValid(packagingType, projectBaseDir);
    isDescriptorFilePresent();
    validateSharedLibraries(sharedLibraries, dependencyProject.getDependencies());
    validateReferencedDomainsIfPresent(dependencyProject.getDependencies());
  }

  /**
   * Validates if a list of dependencies of a mule project conforms to the cardinality restrictions of domains referenced by a
   * mule project.
   * 
   * @throws ValidationException if the condition above does not hold
   */
  protected void validateReferencedDomainsIfPresent(List<ArtifactCoordinates> dependencies) throws ValidationException {
    checkArgument(dependencies != null, "List of dependencies should not be null");
    Set<ArtifactCoordinates> domains = dependencies.stream()
        .filter(d -> StringUtils.equals(MULE_DOMAIN.toString(), d.getClassifier()))
        .collect(Collectors.toSet());
    validateDomain(domains);
  }

  /**
   * Validates if a set of artifact coordinates is a valid set of domains referenced by a mule project. Nevertheless, the set is
   * valid if it is not null, contains at most one element and this element is a mule domain coordinate.
   *
   * @throws ValidationException if at least one of the conditions above does not hold
   */
  protected void validateDomain(Set<ArtifactCoordinates> domains) throws ValidationException {
    checkArgument(domains != null, "Set of domains should not be null");
    if (!domains.stream()
        .allMatch(artifactCoordinates -> StringUtils.equals(artifactCoordinates.getClassifier(), MULE_DOMAIN.toString()))) {
      String message = "Not all dependencies are mule domains";
      throw new ValidationException(message);
    }
    if (domains.size() > MULE_PROJECT_MAXIMUM_NUMBER_OF_DOMAINS) {
      String message =
          "A mule project of type " + packagingType + " should reference at most " + MULE_PROJECT_MAXIMUM_NUMBER_OF_DOMAINS +
              ". However, the project has references to the following domains: "
              + domains.stream().collect(Collectors.toList());
      throw new ValidationException(message);
    }
  }

  /**
   * It validates the project folder structure is valid
   * 
   * @return true if the project's structure is valid
   * @throws ValidationException if the project's structure is invalid
   */
  public static Boolean isProjectStructureValid(String packagingType, Path projectBaseDir) throws ValidationException {
    File mainSrcApplication = mainSrcApplication(packagingType, projectBaseDir);
    if (!mainSrcApplication.exists()) {
      throw new ValidationException("The folder " + mainSrcApplication.getAbsolutePath() + " is mandatory");
    }
    return true;
  }

  /**
   * It validates that the mandatory descriptor files are present
   * 
   * @return true if the project's descriptor files are preset
   * @throws ValidationException if the project's descriptor files are missing
   */
  public Boolean isDescriptorFilePresent() throws ValidationException {
    String errorMessage = "Invalid Mule project. Missing %s file, it must be present in the root of application";
    if (!projectBaseDir.resolve(MULE_ARTIFACT_JSON).toFile().exists()) {
      throw new ValidationException(String.format(errorMessage, MULE_ARTIFACT_JSON));
    }
    return true;
  }

  /**
   * It validates if every shared library is present in the project dependencies.
   *
   * @return true if every shared library is declared in the project dependencies
   * @throws ValidationException if at least one shared library is not defined in the project dependencies
   */
  public void validateSharedLibraries(List<SharedLibraryDependency> sharedLibraries,
                                      List<ArtifactCoordinates> projectDependencies)
      throws ValidationException {
    if (sharedLibraries != null && sharedLibraries.size() != 0) {
      Set<String> projectDependenciesCoordinates = projectDependencies.stream()
          .map(coordinate -> coordinate.getArtifactId() + ":" + coordinate.getGroupId()).collect(Collectors.toSet());
      Set<String> sharedLibrariesCoordinates = sharedLibraries.stream()
          .map(sharedLibrary -> sharedLibrary.getArtifactId() + ":" + sharedLibrary.getGroupId()).collect(Collectors.toSet());

      if (!projectDependenciesCoordinates.containsAll(sharedLibrariesCoordinates)) {
        sharedLibrariesCoordinates.removeAll(projectDependenciesCoordinates);
        throw new ValidationException("The mule application does not contain the following shared libraries: "
            + sharedLibrariesCoordinates.toString());
      }
    }
  }

  private static File mainSrcApplication(String packagingType, Path projectBaseDir) throws ValidationException {
    return PackagingType.fromString(packagingType).getSourceFolderLocation(projectBaseDir).toFile();
  }

}
