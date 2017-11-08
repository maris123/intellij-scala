package org.jetbrains.plugins.hydra.compiler;

import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maris Alexandru
 */
public class HydraCompilerSettingsProfile {
  private String name;
  private List<String> moduleNames = new ArrayList<>();
  private String projectRootPath;
  private HydraCompilerSettings settings;

  public HydraCompilerSettingsProfile(String name, String projectRootPath) {
    this.name = name;
    this.projectRootPath = projectRootPath;
    this.settings = new HydraCompilerSettings(projectRootPath);
  }

  public String getName() {
    return name;
  }

  public void initFrom(HydraCompilerSettingsProfile profile) {
    name = profile.getName();
    settings = profile.getSettings();
    moduleNames = new ArrayList<>(profile.getModuleNames());
    projectRootPath = profile.getProjectRootPath();
  }

  public List<String> getModuleNames() {
    return Collections.unmodifiableList(moduleNames);
  }

  public void addModuleName(String name) {
    moduleNames.add(name);
  }

  public void removeModuleName(String name) {
    moduleNames.remove(name);
  }

  public HydraCompilerSettings getSettings() {
    return settings;
  }

  public void setSettings(HydraCompilerSettings settigns) {
    settings = settigns;
  }

  public String getProjectRootPath() { return projectRootPath; }

  public void setProject(String projectRootPath) { this.projectRootPath = projectRootPath; }

  @Override
  public String toString() {
    return name;
  }
}
