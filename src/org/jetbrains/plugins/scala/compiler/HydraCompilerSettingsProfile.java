package org.jetbrains.plugins.scala.compiler;

import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maris Alexandru
 */
public class HydraCompilerSettingsProfile {
  private String name;
  private List<String> moduleNames = new ArrayList<String>();
  private Project project;
  private HydraCompilerSettings settings;

  public HydraCompilerSettingsProfile(String name, Project project) {
    this.name = name;
    this.project = project;
    this.settings = new HydraCompilerSettings(project);
  }

  public String getName() {
    return name;
  }

  public void initFrom(HydraCompilerSettingsProfile profile) {
    name = profile.getName();
    settings = profile.getSettings();
    moduleNames = new ArrayList<>(profile.getModuleNames());
    project = profile.getProject();
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

  public Project getProject() { return project; }

  public void setProject(Project project) { this.project = project; }

  @Override
  public String toString() {
    return name;
  }
}
