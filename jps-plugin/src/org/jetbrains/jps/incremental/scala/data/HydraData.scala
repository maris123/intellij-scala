package org.jetbrains.jps.incremental.scala.data

import java.io.File

import org.jetbrains.jps.incremental.scala.SettingsManager
import org.jetbrains.jps.model.JpsProject

import scala.collection.JavaConverters._

/**
  * @author Maris Alexandru
  */
case class HydraData(project: JpsProject, files: Map[String, List[File]]) {
  def getCompilerJar(scalaVersion: String): Option[File] = {
    files.get(scalaVersion).getOrElse(List()).find(_.getName.matches(s".*scala-compiler-$scalaVersion-hydra\\d+\\.jar"))
  }

  def getReflectJar(scalaVersion: String): Option[File] = {
    files.get(scalaVersion).getOrElse(List()).find(_.getName.matches(s".*scala-reflect-$scalaVersion-hydra\\d+\\.jar"))
  }

  def otherJars(scalaVersion: String): Seq[File] = {
    files.get(scalaVersion).getOrElse(List()).filterNot(_.getName.contains("scala-compiler"))
  }

  def hydraBridge(scalaVersion: String): Option[File] = {
    files.get(scalaVersion).getOrElse(List()).find(_.getName.matches(s".*hydra-bridge_1_0-${SettingsManager.getHydraSettings(project).getHydraVersion}-sources.jar"))
  }
}

object HydraData {
  def apply(project: JpsProject): HydraData = {
    val files = SettingsManager.getHydraSettings(project).getArtifactPaths.asScala.map { case (key, list) => (key, list.asScala.toList.map(new File(_))) }
    HydraData(project, files.toMap)
  }
}