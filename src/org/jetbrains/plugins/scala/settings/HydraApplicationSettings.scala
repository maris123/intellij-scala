package org.jetbrains.plugins.scala.settings

import java.io.File
import java.util
import scala.collection.JavaConverters._
import com.intellij.openapi.components._

import scala.beans.BeanProperty

/**
  * @author Maris Alexandru
  */
@State(
  name = "HydraApplicationSettings",
  storages = Array(new Storage("hydra_config.xml"))
)
class HydraApplicationSettings extends PersistentStateComponent[HydraApplicationSettingsState]{

  var artifactPaths: Map[String, List[String]] = Map.empty

  override def loadState(state: HydraApplicationSettingsState) = {
    state.removeMapEntriesThatDontExist()
    artifactPaths = state.getGlobalArtifactPaths.asScala.mapValues(_.asScala.toList).toMap
  }

  override def getState = {
    val state = new HydraApplicationSettingsState
    state.setGlobalArtifactPaths(artifactPaths.mapValues(_.asJava).asJava)
    state.removeMapEntriesThatDontExist()
    state
  }

  def putArtifactsFor(scalaVersion: String, hydraVersion: String, artifacts: List[String]): Unit = {
    artifactPaths = artifactPaths + (scalaVersion + "_" + hydraVersion -> artifacts)
  }

  def getArtifactsFor(scalaVersion: String, hydraVersion: String): Option[List[String]] = {
    artifactPaths.get(scalaVersion + "_" + hydraVersion)
  }

  def containsArtifactsFor(scalaVersion: String, hydraVersion: String): Boolean = {
    artifactPaths.contains(scalaVersion  + "_" + hydraVersion)
  }
}

class HydraApplicationSettingsState {
  @BeanProperty
  var globalArtifactPaths: java.util.Map[String, java.util.List[String]] = new java.util.HashMap()

  def removeMapEntriesThatDontExist(): Unit = {
    globalArtifactPaths = globalArtifactPaths.asScala.filter(entry => checkIfArtifactsExist(entry._2)).asJava
  }

  def checkIfArtifactsExist(artifacts: util.List[String]): Boolean = {
    artifacts.asScala.map(new File(_)).forall(_.exists())
  }
}

object HydraApplicationSettings {
  def getInstance(): HydraApplicationSettings = ServiceManager.getService(classOf[HydraApplicationSettings])
}