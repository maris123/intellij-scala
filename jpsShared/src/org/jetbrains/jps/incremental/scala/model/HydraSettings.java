package org.jetbrains.jps.incremental.scala.model;

import org.jetbrains.jps.model.JpsElement;

import java.util.List;

/**
 * @author Maris Alexandru
 */
public interface HydraSettings extends JpsElement {
  boolean isHydraEnabled();
  List<String> getArtifactPaths();
}
