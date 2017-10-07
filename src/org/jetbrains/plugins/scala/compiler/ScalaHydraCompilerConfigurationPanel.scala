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
import scala.util.{Failure, Success}

/**
  * @author Maris Alexandru
  */
class ScalaHydraCompilerConfigurationPanel(project: Project, settings: HydraCompilerSettings) extends HydraCompilerConfigurationPanel {
  private val UnknownVersion = "unknown"

  val documentAdapter = new DocumentAdapter {
      override def textChanged(documentEvent: DocumentEvent): Unit =
        if (getUsername.nonEmpty && getPassword.nonEmpty) downloadButton.setEnabled(true)
        else downloadButton.setEnabled(false)
    }

  val focusListener = new FocusListener {
    override def focusGained(e: FocusEvent) = {}

    override def focusLost(e: FocusEvent) = if (getUsername.nonEmpty && getPassword.nonEmpty &&
      (HydraCredentialsManager.getLogin != getUsername || HydraCredentialsManager.getPlainPassword != getPassword)) {
      HydraCredentialsManager.setCredentials(getUsername, getPassword)
      hydraVersionComboBox.setItems(Versions.loadScalaVersions(Platform.Hydra))
    }
  }

  userTextField.addFocusListener(focusListener)
  userTextField.getDocument.addDocumentListener(documentAdapter)
  passwordTextField.getDocument.addDocumentListener(documentAdapter)
  passwordTextField.addFocusListener(focusListener)
  hydraVersionComboBox.setItems(Versions.loadScalaVersions(Platform.Hydra))
  downloadButton.addActionListener((_: ActionEvent) => onDownload())

  def selectedVersion: String = hydraVersionComboBox.getSelectedItem.asInstanceOf[String]

  def onDownload() = {
    downloadVersionWithProgress(project.scalaModules.map(module => module.sdk.compilerVersion.getOrElse(UnknownVersion)), selectedVersion)
    settings.hydraVersion = selectedVersion
  }

  private def downloadVersionWithProgress(scalaVersions: Seq[String], hydraVersion: String): Unit = {
    val filteredScalaVersions = for {
      rawVersion <- scalaVersions.distinct
      if rawVersion != UnknownVersion && rawVersion != "2.12.0"
      version = Version(rawVersion)
      if version >= Version("2.11")
      filteredVersion = version.presentation
    } yield filteredVersion

    val filteredScalaVersionsString = filteredScalaVersions.mkString(", ")
    val scalaVersionsToBeDownloaded = filteredScalaVersions.filterNot(settings.artifactPaths.containsKey(_))
    val scalaVersionsToBeDownloadedString = scalaVersionsToBeDownloaded.mkString(", ")
    if (!scalaVersionsToBeDownloaded.isEmpty) {
      val result = extensions.withProgressSynchronouslyTry(s"Downloading Hydra $hydraVersion for $scalaVersionsToBeDownloadedString")(downloadVersion(scalaVersionsToBeDownloaded, hydraVersion))
      result match {
        case Failure(exception) => {
          Messages.showErrorDialog(contentPanel, exception.getMessage, s"Error Downloading Hydra $hydraVersion for $scalaVersionsToBeDownloadedString")
        }
        case Success(_) => Messages.showInfoMessage(s"Successfully downloaded Hydra $hydraVersion for $scalaVersionsToBeDownloadedString", "Download Hydra Successful")
      }
    } else {
      Messages.showInfoMessage(s"Hydra $hydraVersion for $filteredScalaVersionsString is already downloaded", "Hydra version already downloaded")
    }
  }

  private def downloadVersion(scalaVersions: Seq[String], hydraVersion: String):(((String) => Unit) => Unit) =
    (listener: (String) => Unit) => scalaVersions.foreach(version =>
      settings.artifactPaths.put(version,HydraArtifactsCache.getOrDownload(version, hydraVersion, listener).asJava))
}
