package org.jetbrains.plugins.scala.compiler

import java.io.File

import com.intellij.openapi.components.{PersistentStateComponent, ServiceManager, State, Storage}
import com.intellij.openapi.project.Project

import scala.beans.BeanProperty

/**
  * @author Maris Alexandru
  */
@State(
  name = "HydraSettings",
  storages = Array(new Storage("hydra.xml"))
)
class HydraCompilerConfiguration(project: Project) extends PersistentStateComponent[HydraCompilerSettingsState] {

  private val ProjectRoot: String = getProjectRootPath

  var isHydraEnabled: Boolean = false

  var hydraVersion: String = "0.9.5"

  override def getState: HydraCompilerSettingsState = {
    val state = new HydraCompilerSettingsState()
    state.hydraVersion = hydraVersion
    state.isHydraEnabled = isHydraEnabled
    state.projectRoot = ProjectRoot
    state
  }

  override def loadState(state: HydraCompilerSettingsState): Unit = {
    isHydraEnabled = state.isHydraEnabled
    hydraVersion = state.hydraVersion
  }

  def getDefaultHydraStorePath: String = ProjectRoot + File.separator + ".hydra"

  private def getProjectRootPath: String = project.getBaseDir.getPresentableUrl
}

object HydraCompilerConfiguration {
  def getInstance(project: Project): HydraCompilerConfiguration = ServiceManager.getService(project, classOf[HydraCompilerConfiguration])
}

class HydraCompilerSettingsState {
  @BeanProperty
  var isHydraEnabled: Boolean = false

  @BeanProperty
  var hydraVersion: String = ""

  @BeanProperty
  var noOfCores: String = ""

  @BeanProperty
  var hydraStorePath: String = ""

  @BeanProperty
  var sourcePartitioner: String = ""

  @BeanProperty
  var projectRoot: String = ""
}


