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
        if (!getUsername.isEmpty && !getPassword.isEmpty) downloadButton.setEnabled(true)
        else downloadButton.setEnabled(false)
    }

  val focusListener = new FocusListener {
    override def focusGained(e: FocusEvent) = {}

    override def focusLost(e: FocusEvent) = if (!getUsername.isEmpty && !getPassword.isEmpty &&
      (HydraCredentialsManager.getLogin != getUsername || HydraCredentialsManager.getPlainPassword != getPassword)) {
      HydraCredentialsManager.setCredentials(getUsername, getPassword)
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
    downloadVersionWithProgress(project.scalaModules.map(module => module.sdk.compilerVersion.getOrElse(UnknownVersion)), selectedVersion)
    settings.hydraVersion = selectedVersion
  }

  private def downloadVersionWithProgress(scalaVersions: Seq[String], hydraVersion: String): Unit = {
    val filteredScalaVersions = scalaVersions.distinct.filterNot(_ == UnknownVersion)
      .map(Version(_)).filter(_ >= Version("2.11")).map(_.presentation).filterNot(_ == "2.12.0")
    val filteredScalaVersionsString = filteredScalaVersions.mkString(", ")
    val scalaVersionsToBeDownloaded = filteredScalaVersions.filterNot(settings.artifactPaths.containsKey(_))
    val scalaVersionsToBeDownloadedString = scalaVersionsToBeDownloaded.mkString(", ")
    if (!scalaVersionsToBeDownloaded.isEmpty) {
      val result = extensions.withProgressSynchronouslyTry(s"Downloading Hydra $hydraVersion for $scalaVersionsToBeDownloadedString")(downloadVersion(scalaVersionsToBeDownloaded, hydraVersion))
      result match {
        case Failure(exception) => {
          Messages.showErrorDialog(myContentPanel, exception.getMessage, s"Error Downloading Hydra $hydraVersion for $scalaVersionsToBeDownloadedString")
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
