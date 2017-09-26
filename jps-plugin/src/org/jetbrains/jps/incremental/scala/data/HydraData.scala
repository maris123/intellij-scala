package org.jetbrains.jps.incremental.scala.data

import java.io.File

import org.jetbrains.jps.incremental.scala.SettingsManager
import org.jetbrains.jps.model.JpsProject

import scala.collection.JavaConverters._

/**
  * @author Maris Alexandru
  */
case class HydraData(project: JpsProject, files: List[File]) {
  def getCompilerJar(): Option[File] = {
    files.find(_.getName.matches(".*scala-compiler-2.11.8-hydra\\d\\d\\.jar"))
  }

  def getReflectJar(): Option[File] = {
    files.find(_.getName.matches(".*scala-reflect-2.11.8-hydra\\d\\d\\.jar"))
  }

  def otherFiles(): Seq[File] = {
    files.filterNot(_.getName.contains("scala-compiler"))
  }
}

object HydraData {
  def apply(project: JpsProject): HydraData = {
    val files = SettingsManager.getHydraSettings(project).getArtifactPaths.asScala.toList.map(path => new File(path))
    HydraData(project, files)
  }
}