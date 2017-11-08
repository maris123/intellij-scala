package org.jetbrains.plugins.hydra.compiler

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components._
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

  var isHydraSettingsEnabled: Boolean = false

  var isHydraEnabled: Boolean = false

  var hydraVersion: String = ""

  var defaultProfile: HydraCompilerSettingsProfile = new HydraCompilerSettingsProfile("Default", getProjectRootPath)

  var customProfiles: Seq[HydraCompilerSettingsProfile] = Seq.empty

  override def getState: HydraCompilerSettingsState = {
    val state = new HydraCompilerSettingsState()
    state.hydraVersion = hydraVersion
    state.isHydraEnabled = isHydraEnabled
    state.projectRoot = ProjectRoot
    state.isHydraSettingsEnabled = isHydraSettingsEnabled
    state
  }

  override def loadState(state: HydraCompilerSettingsState): Unit = {
    isHydraEnabled = state.isHydraEnabled
    hydraVersion = state.hydraVersion
    isHydraSettingsEnabled = state.isHydraSettingsEnabled
  }

  def getProjectRootPath: String = project.getBaseDir.getPresentableUrl
}

object HydraCompilerConfiguration {
  def getInstance(project: Project): HydraCompilerConfiguration = ServiceManager.getService(project, classOf[HydraCompilerConfiguration])
}

class HydraCompilerSettingsState {
  @BeanProperty
  var isHydraEnabled: Boolean = false

  @BeanProperty
  var isHydraSettingsEnabled: Boolean = false

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
