package org.jetbrains.plugins.scala.caches

import java.util.concurrent.ConcurrentHashMap

import org.jetbrains.plugins.scala.project.Platform
import org.jetbrains.plugins.scala.project.template.Downloader

/**
  * @author Maris Alexandru
  */
object HydraArtifactsCache {
  private val cache = new ConcurrentHashMap[(String, String), Seq[String]]()
  private val SPLIT_REGEX = "\\[info\\] \\* Attributed\\(|\\)"
  private val HYDRA_REGEX = ".*\\\\hydra_\\d+\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\.jar"
  private val VERSIONS_REGEX = ".*\\\\hydra_(\\d+\\.\\d+\\.\\d+)-(\\d+\\.\\d+\\.\\d+)\\.jar".r

  private def cacheArtifacts(artifacts: String): Unit = {
    val paths = artifacts.split("\n").filter(s => s.contains("Attributed")).map(s => s.split(SPLIT_REGEX)(1))
    val hydraPath = paths.filter(s => s.matches(HYDRA_REGEX))(0)
    val VERSIONS_REGEX(scalaVersion, hydraVersion) = hydraPath
    cache.put((scalaVersion, hydraVersion), paths)
  }

  def getOrDownload(scalaVersion: String, hydraVersion: String,listener: (String) => Unit) = {
    val artifacts = cache.getOrDefault((scalaVersion, hydraVersion), Seq.empty)

    if(artifacts.isEmpty) {
      val stringBuilder = new StringBuilder
      Downloader.downloadScala(Platform.Hydra, scalaVersion, (text: String) => {
        stringBuilder.append(text)
        listener(text)
      })
      cacheArtifacts(stringBuilder.toString)
      cache.get((scalaVersion, hydraVersion))
    } else {
      artifacts
    }
  }
}
