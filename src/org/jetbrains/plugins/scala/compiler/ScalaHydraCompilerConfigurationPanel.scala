package org.jetbrains.plugins.scala.compiler

import java.awt.event.{ActionEvent, FocusEvent, FocusListener}
import javax.swing.event.DocumentEvent

import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.scala.caches.HydraArtifactsCache
import org.jetbrains.plugins.scala.extensions
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import org.jetbrains.plugins.scala.project.{Platform, ProjectExt, Version, Versions}

import scala.collection.JavaConverters._
import scala.util.Failure

/**
  * @author Maris Alexandru
  */
class ScalaHydraCompilerConfigurationPanel(project: Project, settings: HydraCompilerSettings) extends HydraCompilerConfigurationPanel {
  private val UNKNOWN_VERSION = "unknown"

  val documentAdapter = new DocumentAdapter {
      override def textChanged(documentEvent: DocumentEvent): Unit =
        if (!getUsername.isEmpty && !getPassword.isEmpty) downloadButton.setEnabled(true)
        else downloadButton.setEnabled(false)
    }

  val focusListener = new FocusListener {
    override def focusGained(e: FocusEvent) = {}

    override def focusLost(e: FocusEvent) = if (!getUsername.isEmpty && !getPassword.isEmpty) {
      HydraCredentialsManager.setLogin(getUsername)
      HydraCredentialsManager.setPlainPassword(getPassword)
      myVersion.setItems(Versions.loadScalaVersions(Platform.Hydra))
    }
  }

  myTextField1.addFocusListener(focusListener)
  myTextField1.getDocument.addDocumentListener(documentAdapter)
  myPasswordField1.getDocument.addDocumentListener(documentAdapter)
  myPasswordField1.addFocusListener(focusListener)
  myVersion.setItems(Versions.loadScalaVersions(Platform.Hydra))
  downloadButton.addActionListener((_: ActionEvent) => onDownload())

  def selectedVersion: String = myVersion.getSelectedItem.asInstanceOf[String]

  def onDownload() = {
    HydraCredentialsManager.setLogin(getUsername)
    HydraCredentialsManager.setPlainPassword(getPassword)
    downloadVersionWithProgress(project.scalaModules.map(module => module.sdk.compilerVersion.getOrElse(UNKNOWN_VERSION)), selectedVersion)
    settings.hydraVersion = selectedVersion
  }

  private def downloadVersionWithProgress(scalaVersions: Seq[String], hydraVersion: String): Unit = {
    val filteredScalaVersions = scalaVersions.filterNot(_ == UNKNOWN_VERSION)
      .map(Version(_)).filter(_ >= Version("2.11")).map(_.presentation).filterNot(_ == "2.12.0").distinct
    val result = extensions.withProgressSynchronouslyTry(s"Downloading Hydra $hydraVersion for ${filteredScalaVersions.mkString(", ")}")(downloadVersion(filteredScalaVersions, hydraVersion))
    result match {
      case Failure(exception) => {
        Messages.showErrorDialog(myContentPanel, exception.getMessage, s"Error Downloading Hydra $hydraVersion for ${filteredScalaVersions.mkString(", ")}")
      }
      case _ =>
    }
  }

  private def downloadVersion(scalaVersions: Seq[String], hydraVersion: String):(((String) => Unit) => Unit) =
    (listener: (String) => Unit) => scalaVersions.foreach(version =>
      settings.artifactPaths.put(version,HydraArtifactsCache.getOrDownload(version, hydraVersion, listener).asJava))
}
