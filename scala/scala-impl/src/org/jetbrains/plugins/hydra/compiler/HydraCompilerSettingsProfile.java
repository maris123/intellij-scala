package org.jetbrains.plugins.hydra.compiler;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maris Alexandru
 */
public class HydraCompilerSettingsProfile {
  private String name;
  private List<String> moduleNames = new ArrayList<>();
  private HydraCompilerSettings settings;

  public HydraCompilerSettingsProfile(String name) {
    this.name = name;
    this.settings = new HydraCompilerSettings();
  }

  public String getName() {
    return name;
  }

  public void initFrom(HydraCompilerSettingsProfile profile) {
    name = profile.getName();
    settings = profile.getSettings();
    moduleNames = new ArrayList<>(profile.getModuleNames());
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

  @Override
  public String toString() {
    return name;
  }
}
