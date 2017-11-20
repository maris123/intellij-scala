package org.jetbrains.jps.incremental.scala;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.scala.model.CompilerSettingsImpl;
import org.jetbrains.jps.incremental.scala.model.GlobalHydraSettingsImpl;
import org.jetbrains.jps.incremental.scala.model.HydraCompilerSettingsImpl;
import org.jetbrains.jps.incremental.scala.model.HydraSettingsImpl;
import org.jetbrains.jps.model.JpsGlobal;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.serialization.JpsGlobalExtensionSerializer;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maris Alexandru
 */
public class HydraSerializerService extends JpsModelSerializerExtension {

  @NotNull
  @Override
  public List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Collections.singletonList(new HydraSettingsSerializer());
  }

  @NotNull
  @Override
  public List<? extends JpsGlobalExtensionSerializer> getGlobalExtensionSerializers() {
    return Collections.singletonList(new GlobalHydraSettingsSerializer());
  }

  private static class HydraSettingsSerializer extends JpsProjectExtensionSerializer {
    private HydraSettingsSerializer() {
      super("hydra.xml", "HydraSettings");
    }

    @Override
    public void loadExtension(@NotNull JpsProject jpsProject, @NotNull Element componentTag) {
      HydraSettingsImpl.State state = getHydraSettingsState(componentTag);

      HydraCompilerSettingsImpl defaultSettings = loadSettings(componentTag);

      Map<String, String> moduleToProfile = new HashMap<String, String>();
      Map<String, HydraCompilerSettingsImpl> profileToSettings = new HashMap<String, HydraCompilerSettingsImpl>();

      for (Element profileElement : componentTag.getChildren("profile")) {
        String profile = profileElement.getAttributeValue("name");
        HydraCompilerSettingsImpl settings = loadSettings(profileElement);
        profileToSettings.put(profile, settings);

        List<String> modules = StringUtil.split(profileElement.getAttributeValue("modules"), ",");
        for (String module : modules) {
          moduleToProfile.put(module, profile);
        }
      }

      HydraSettingsImpl settings = new HydraSettingsImpl(state == null ? new HydraSettingsImpl.State() : state, defaultSettings, profileToSettings, moduleToProfile);
      SettingsManager.setHydraSettings(jpsProject, settings);
    }

    @Override
    public void saveExtension(@NotNull JpsProject jpsProject, @NotNull Element componentTag) {
      // do nothing
    }

    private static HydraCompilerSettingsImpl loadSettings(Element componentTag) {
      HydraCompilerSettingsImpl.State state = XmlSerializer.deserialize(componentTag, HydraCompilerSettingsImpl.State.class);
      return new HydraCompilerSettingsImpl(state == null ? new HydraCompilerSettingsImpl.State() : state);
    }

    private HydraSettingsImpl.State getHydraSettingsState(Element componentTag) {
      Element stateConfiguration = componentTag.getChild("HydraCompilerConfigurationState");
      HydraSettingsImpl.State state = new HydraSettingsImpl.State();

      if (stateConfiguration != null) {
        state = XmlSerializer.deserialize(stateConfiguration, HydraSettingsImpl.State.class);
      }

      return state;
    }
  }

  private static class GlobalHydraSettingsSerializer extends JpsGlobalExtensionSerializer {
    private GlobalHydraSettingsSerializer() { super("hydra_config.xml", "HydraApplicationSettings");}

    @Override
    public void loadExtension(@NotNull JpsGlobal jpsGlobal, @NotNull Element componentTag) {
      GlobalHydraSettingsImpl.State state = XmlSerializer.deserialize(componentTag, GlobalHydraSettingsImpl.State.class);
      GlobalHydraSettingsImpl settings = new GlobalHydraSettingsImpl(state == null ? new GlobalHydraSettingsImpl.State() : state);
      SettingsManager.setGlobalHydraSettings(jpsGlobal, settings);
    }

    @Override
    public void saveExtension(@NotNull JpsGlobal jpsGlobal, @NotNull Element componentTag) {
      // do nothing
    }
  }
}
