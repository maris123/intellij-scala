package org.jetbrains.jps.incremental.scala.model;

import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsElementBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maris Alexandru
 */
public class HydraCompilerSettingsImpl extends JpsElementBase<HydraCompilerSettingsImpl>  implements HydraCompilerSettings {
  public static final HydraCompilerSettingsImpl DEFAULT = new HydraCompilerSettingsImpl(new State());

  private State state;

  public HydraCompilerSettingsImpl(State state) {
    this.state = state;
  }

  @Override
  public String[] getCompilerOptions() {
    List<String> list = new ArrayList<String>();

    if (state.noOfCores != null) {
      list.add("-cpus");
      list.add(state.noOfCores);
    }

    if (state.sourcePartitioner != null) {
      list.add("-YsourcePartitioner:" + state.sourcePartitioner);
    }

    return list.toArray(new String[list.size()]);
  }

  @NotNull
  @Override
  public HydraCompilerSettingsImpl createCopy() {
    return new HydraCompilerSettingsImpl(XmlSerializerUtil.createCopy(state));
  }

  @Override
  public void applyChanges(@NotNull HydraCompilerSettingsImpl hydraCompilerSettings) {
    // do nothing
  }

  public static class State {
    public String noOfCores;
    public String sourcePartitioner;
  }
}
