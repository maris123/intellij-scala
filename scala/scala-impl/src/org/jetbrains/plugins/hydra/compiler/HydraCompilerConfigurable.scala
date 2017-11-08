package org.jetbrains.plugins.hydra.compiler

import javax.swing.JPanel

import com.intellij.compiler.server.BuildManager
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorNotifications
import org.jetbrains.plugins.hydra.settings.HydraApplicationSettings
import org.jetbrains.plugins.scala.project.AbstractConfigurable
import scala.collection.JavaConverters._

/**
  * @author Maris Alexandru
  */
class HydraCompilerConfigurable (project: Project) extends AbstractConfigurable("Hydra Compiler"){
  private val settings = HydraCompilerConfiguration.getInstance(project)
  private val hydraGlobalSettings = HydraApplicationSettings.getInstance()
  private val form = new ScalaHydraCompilerConfigurationPanel(project, settings, hydraGlobalSettings)

  private val profiles = form.getHydraProfilesPanel

  override def createComponent(): JPanel = form.getContentPanel

  override def isModified: Boolean = form.isHydraEnabled != settings.isHydraEnabled ||
    form.getUsername != HydraCredentialsManager.getLogin ||
    form.getPassword != HydraCredentialsManager.getPlainPassword ||
    form.selectedVersion != settings.hydraVersion ||
    form.getHydraRepository != hydraGlobalSettings.getHydraRepositoryUrl ||
    form.getHydraRepositoryRealm != hydraGlobalSettings.hydraRepositoryRealm ||
    profiles.getDefaultProfile.getSettings != settings.defaultProfile.getSettings ||
    !profiles.getModuleProfiles.asScala.corresponds(settings.customProfiles)(_.getSettings == _.getSettings)

  override def reset() {
    form.setUsername(HydraCredentialsManager.getLogin)
    form.setPassword(HydraCredentialsManager.getPlainPassword)
    form.setIsHydraEnabled(settings.isHydraEnabled)
    form.setSelectedVersion(settings.hydraVersion)
    form.setHydraRepository(hydraGlobalSettings.getHydraRepositoryUrl)
    form.setHydraRepositoryRealm(hydraGlobalSettings.hydraRepositoryRealm)
    profiles.initProfiles(settings.defaultProfile, settings.customProfiles.asJava)
  }

  override def apply() {
    settings.defaultProfile = profiles.getDefaultProfile
    settings.customProfiles = profiles.getModuleProfiles.asScala
    settings.hydraVersion = form.selectedVersion
    settings.isHydraEnabled = form.isHydraEnabled
    hydraGlobalSettings.setHydraRepositopryUrl(form.getHydraRepository)
    hydraGlobalSettings.hydraRepositoryRealm = form.getHydraRepositoryRealm
    HydraCredentialsManager.setCredentials(form.getUsername, form.getPassword)
    EditorNotifications.updateAll()
    BuildManager.getInstance().clearState(project)
  }
}