package org.jetbrains.jps.incremental.scala.model;

import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.model.ex.JpsElementBase;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maris Alexandru
 */
public class HydraSettingsImpl extends JpsElementBase<HydraSettingsImpl> implements HydraSettings{
  public static final HydraSettings DEFAULT = new HydraSettingsImpl(new State(), HydraCompilerSettingsImpl.DEFAULT,
    new HashMap<String, HydraCompilerSettingsImpl>(), new HashMap<String, String>());

  private final State state;

  private HydraCompilerSettingsImpl myDefaultSettings;

  private Map<String, HydraCompilerSettingsImpl> myProfileToSettings;

  private Map<String, String> myModuleToProfile;

  public HydraSettingsImpl(State state, HydraCompilerSettingsImpl defaultSettings, Map<String, HydraCompilerSettingsImpl> profileToSettings, Map<String, String> moduleToProfile) {
    this.state = state;
    this.myDefaultSettings = defaultSettings;
    this.myProfileToSettings = profileToSettings;
    this.myModuleToProfile = moduleToProfile;
  }

  @Override
  public boolean isHydraEnabled() { return state.isHydraEnabled; }

  @Override
  public String getHydraVersion() {
    return state.hydraVersion;
  }

  @Override
  public String getHydraStorePath() { return Paths.get(state.hydraStorePath).toString(); }

  @Override
  public String getProjectRoot() { return Paths.get(state.projectRoot).toString(); }

  @Override
  public HydraCompilerSettings getCompilerSettings(ModuleChunk chunk) {
    String module = chunk.representativeTarget().getModule().getName();
    String profile = myModuleToProfile.get(module);
    return profile == null ? myDefaultSettings : myProfileToSettings.get(profile);
  }

  @NotNull
  @Override
  public HydraSettingsImpl createCopy() {
    HydraCompilerSettingsImpl defaultSettings = myDefaultSettings.createCopy();

    Map<String, HydraCompilerSettingsImpl> profileToSettings = new HashMap<String, HydraCompilerSettingsImpl>();
    for (Map.Entry<String, HydraCompilerSettingsImpl> entry : myProfileToSettings.entrySet()) {
      profileToSettings.put(entry.getKey(), entry.getValue().createCopy());
    }

    HashMap<String, String> moduleToProfile = new HashMap<String, String>(myModuleToProfile);

    return new HydraSettingsImpl(XmlSerializerUtil.createCopy(state), defaultSettings, profileToSettings, moduleToProfile);
  }

  @Override
  public void applyChanges(@NotNull HydraSettingsImpl hydraSettings) {
    //do nothing
  }

  public static class State {
    public boolean isHydraEnabled = false;
    public String hydraVersion = "";
    //public String noOfCores = Integer.toString((int) Math.ceil(Runtime.getRuntime().availableProcessors()/2D));
    public String hydraStorePath = "";
    //public String sourcePartitioner = "";
    public String projectRoot = "";
  }
}
