package org.jetbrains.plugins.hydra.compiler

import java.nio.file.Paths

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components._
import com.intellij.openapi.project.Project
import com.intellij.openapi.module.Module
import com.intellij.util.xmlb.{SkipDefaultValuesSerializationFilters, XmlSerializer}
import org.jdom.Element

import scala.collection.JavaConverters._
import scala.beans.BeanProperty

/**
  * @author Maris Alexandru
  */
@State(
  name = "HydraSettings",
  storages = Array(new Storage("hydra.xml"))
)
class HydraCompilerConfiguration(project: Project) extends PersistentStateComponent[Element] {

  private val ProjectRoot: String = getProjectRootPath

  var isHydraSettingsEnabled: Boolean = false

  var isHydraEnabled: Boolean = false

  var hydraVersion: String = ""

  var defaultProfile: HydraCompilerSettingsProfile = new HydraCompilerSettingsProfile("Default")

  var customProfiles: Seq[HydraCompilerSettingsProfile] = Seq.empty

  var hydraLogLocation: String = Paths.get(getDefaultHydraStorePath, "hydra.log").toString

  var hydraStorePath: String = getDefaultHydraStorePath

  def getSettingsForModule(module: Module): HydraCompilerSettings = {
    val profile = customProfiles.find(_.getModuleNames.contains(module.getName)).getOrElse(defaultProfile)
    profile.getSettings
  }

  override def getState: Element = {
    val configurationElement = XmlSerializer.serialize(defaultProfile.getSettings.getState)
    val projectSettingsElement = XmlSerializer.serialize(getSettingsState)
    configurationElement.addContent(projectSettingsElement)

    customProfiles.foreach { profile =>
      val profileElement = XmlSerializer.serialize(profile.getSettings.getState)
      profileElement.setName("profile")
      profileElement.setAttribute("name", profile.getName)
      profileElement.setAttribute("modules", profile.getModuleNames.asScala.mkString(","))

      configurationElement.addContent(profileElement)
    }

    configurationElement
  }

  override def loadState(configurationElement: Element): Unit = {
    val state = XmlSerializer.deserialize(configurationElement, classOf[HydraCompilerConfigurationState])
    setFromState(state)

    defaultProfile.setSettings(new HydraCompilerSettings(XmlSerializer.deserialize(configurationElement, classOf[HydraCompilerSettingsState])))

    customProfiles = configurationElement.getChildren("profile").asScala.map { profileElement =>
      val profile = new HydraCompilerSettingsProfile(profileElement.getAttributeValue("name"))

      val settings = XmlSerializer.deserialize(profileElement, classOf[HydraCompilerSettingsState])
      profile.setSettings(new HydraCompilerSettings(settings))

      val moduleNames = profileElement.getAttributeValue("modules").split(",").filter(!_.isEmpty)
      moduleNames.foreach(profile.addModuleName)

      profile
    }
  }

  private def getSettingsState: HydraCompilerConfigurationState = {
    val state = new HydraCompilerConfigurationState
    state.setIsHydraEnabled(isHydraEnabled)
    state.setIsHydraSettingsEnabled(isHydraSettingsEnabled)
    state.setHydraVersion(hydraVersion)
    state.setHydraStorePath(hydraStorePath)
    state.setProjectRoot(ProjectRoot)

    state
  }

  private def setFromState(state: HydraCompilerConfigurationState): Unit = {
    isHydraEnabled = state.getIsHydraEnabled
    isHydraSettingsEnabled = state.getIsHydraSettingsEnabled
    hydraVersion = state.getHydraVersion
    hydraStorePath = state.getHydraStorePath
  }

  def getProjectRootPath: String = project.getBaseDir.getPresentableUrl

  def getDefaultHydraStorePath: String = Paths.get(ProjectRoot, ".hydra", "idea").toString
}

object HydraCompilerConfiguration {
  def getInstance(project: Project): HydraCompilerConfiguration = ServiceManager.getService(project, classOf[HydraCompilerConfiguration])
}

class HydraCompilerConfigurationState {
  @BeanProperty
  var isHydraEnabled: Boolean = false

  @BeanProperty
  var isHydraSettingsEnabled: Boolean = false

  @BeanProperty
  var hydraVersion: String = ""

  @BeanProperty
  var hydraStorePath: String = ""

  @BeanProperty
  var projectRoot: String = ""
}
