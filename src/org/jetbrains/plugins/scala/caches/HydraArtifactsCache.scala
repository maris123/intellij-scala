package org.jetbrains.plugins.scala.caches

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.plugins.scala.project.Platform
import org.jetbrains.plugins.scala.project.template.Downloader

/**
  * @author Maris Alexandru
  */
object HydraArtifactsCache {
  private val LOG = Logger.getInstance(this.getClass)
  private val cache = new ConcurrentHashMap[(String, String), Seq[String]]()
  private val SplitRegex = "\\* Attributed\\(|\\)".r
  private val ArtifactsRegex = "\\* Attributed\\(.*\\)".r

  private def cacheArtifacts(artifacts: String, scalaVersion: String, hydraVersion: String) = {
    val paths = artifacts.split("\n").filter(s => ArtifactsRegex.findFirstIn(s).nonEmpty).map(s => SplitRegex.split(s)(1))
    val hydraBridge = s"${env("IVY_HOME").getOrElse(System.getProperty("user.home") + "/.ivy2")}/cache/com.triplequote/hydra-bridge_1_0/srcs/hydra-bridge_1_0-$hydraVersion-sources.jar"
    val artifactPaths = hydraBridge +: paths
    cache.put((scalaVersion, hydraVersion), artifactPaths)
    artifactPaths
  }

  def getOrDownload(scalaVersion: String, hydraVersion: String, listener: (String) => Unit): Seq[String] = {
    val artifacts = cache.getOrDefault((scalaVersion, hydraVersion), Seq.empty)

    if (artifacts.isEmpty) {
      val stringBuilder = new StringBuilder
      Downloader.downloadHydra(s"${scalaVersion}_$hydraVersion", (text: String) => {
        stringBuilder.append(text)
        listener(text)
      })
      LOG.info(stringBuilder.toString())
      cacheArtifacts(stringBuilder.toString, scalaVersion, hydraVersion)
    } else {
      artifacts
    }
  }

  private def env(name: String): Option[String] = Option(System.getenv(name))
}
