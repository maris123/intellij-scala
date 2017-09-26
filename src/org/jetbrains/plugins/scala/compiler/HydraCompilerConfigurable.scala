package org.jetbrains.plugins.scala.compiler

import java.lang.String.format
import javax.swing.JPanel

import com.intellij.compiler.server.BuildManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.project.ProjectExt
import org.jetbrains.plugins.scala.caches.HydraArtifactsCache
import org.jetbrains.plugins.scala.extensions
import org.jetbrains.plugins.scala.project.AbstractConfigurable

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}


/**
  * @author Maris Alexandru
  */
class HydraCompilerConfigurable (project: Project, settings: HydraCompilerSettings) extends AbstractConfigurable("Hydra Compiler"){
  private val form = new HydraCompilerConfigurationPanel()

  override def createComponent(): JPanel = form.getContentPanel

  override def isModified: Boolean = form.isHydraEnabled != settings.isHydraEnabled ||
          form.getUsername != HydraCredentialsManager.getLogin ||
          form.getPassword != HydraCredentialsManager.getPlainPassword

  override def reset() {
    form.setUsername(HydraCredentialsManager.getLogin)
    form.setPassword(HydraCredentialsManager.getPlainPassword)
    form.setIsHydraEnabled(settings.isHydraEnabled)
  }

  override def apply() {
    BuildManager.getInstance().clearState(project)
    project.scalaModules.foreach(module => println(module.sdk.compilerVersion))
    settings.isHydraEnabled = form.isHydraEnabled
    if(form.isHydraEnabled) {
      settings.artifactPaths = downloadVersionWithProgress("2.11.8", "0.9.3").asJava
    }
    settings.hydraVersion = "0.9.3"
    HydraCredentialsManager.setLogin(form.getUsername)
    HydraCredentialsManager.setPlainPassword(form.getPassword)
  }

  private def downloadVersionWithProgress(scalaVersion: String, hydraVersion: String): Seq[String] = {
    val result = extensions.withProgressSynchronouslyTry(format("Downloading Hydra %s for %s", hydraVersion, scalaVersion))(downloadVersion(scalaVersion, hydraVersion))
    result match {
      case Failure(exception) => {
        Messages.showErrorDialog(form.getContentPanel, exception.getMessage, format("Error Downloading Hydra %s for %s", hydraVersion, scalaVersion))
        Seq.empty
      }
      case Success(artifacts) => artifacts
    }
  }

  private def downloadVersion(scalaVersion: String, hydraVersion: String):(((String) => Unit) => Seq[String]) =
    ((listener: (String) => Unit) => HydraArtifactsCache.getOrDownload(scalaVersion, hydraVersion, listener))
}