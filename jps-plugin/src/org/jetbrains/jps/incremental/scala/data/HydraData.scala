package org.jetbrains.jps.incremental.scala.data

import java.io.File

import org.jetbrains.jps.incremental.scala.SettingsManager
import org.jetbrains.jps.model.JpsProject

import scala.collection.JavaConverters._

/**
  * @author Maris Alexandru
  */
class HydraData(project: JpsProject, files: List[File], scalaVersion: String) {
  private val HydraCompilerRegex = s".*scala-compiler-$scalaVersion-hydra\\d+\\.jar".r
  private val HydraReflectRegex = s".*scala-reflect-$scalaVersion-hydra\\d+\\.jar".r
  private val HydraBridgeRegex = s".*${HydraData.HydraBridgeName}-${SettingsManager.getHydraSettings(project).getHydraVersion}-sources.jar".r

  def getCompilerJar: Option[File] = files.find(file => HydraCompilerRegex.findFirstIn(file.getName).nonEmpty)

  def getReflectJar: Option[File] = files.find(file => HydraReflectRegex.findFirstIn(file.getName).nonEmpty)

  def otherJars: List[File] = files.filterNot(_.getName.contains("scala-compiler"))

  def hydraBridge: Option[File] = files.find(file => HydraBridgeRegex.findFirstIn(file.getName).nonEmpty)
}

object HydraData {
  val HydraBridgeName = "hydra-bridge_1_0"

  def apply(project: JpsProject, scalaVersion: String): HydraData = {
    val files = SettingsManager.getHydraSettings(project).getArtifactPaths.getOrDefault(scalaVersion, List[String]().asJava).asScala.map(new File(_)).toList

    new HydraData(project, files, scalaVersion)
  }
}