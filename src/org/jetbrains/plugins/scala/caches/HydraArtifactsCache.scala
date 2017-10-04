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
  private val SplitRegex = "\\[info\\] \\* Attributed\\(|\\)"
  private val ArtifactsRegex = "\\[info\\] \\* Attributed\\(.*\\)"
  private val HydraRegex = ".*hydra_(\\d+\\.\\d+\\.\\d+)-(\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?)\\.jar".r

  private def cacheArtifacts(artifacts: String): Unit = {
    val paths = artifacts.split("\n").filter(s => s.matches(ArtifactsRegex)).map(s => s.split(SplitRegex)(1))
    val hydraPath = paths.filter(s => s.matches(HydraRegex.regex))(0)
    val HydraRegex(scalaVersion, hydraVersion, _) = hydraPath
    val hydraBridge = s"${System.getProperty("user.home")}/.ivy2/cache/com.triplequote/hydra-bridge_1_0/srcs/hydra-bridge_1_0-$hydraVersion-sources.jar"
    cache.put((scalaVersion, hydraVersion), hydraBridge +: paths)
  }

  def getOrDownload(scalaVersion: String, hydraVersion: String,listener: (String) => Unit) = {
    val artifacts = cache.getOrDefault((scalaVersion, hydraVersion), Seq.empty)

    if(artifacts.isEmpty) {
      val stringBuilder = new StringBuilder
      Downloader.downloadScala(Platform.Hydra, s"${scalaVersion}_$hydraVersion", (text: String) => {
        stringBuilder.append(text)
        listener(text)
      })
      LOG.info(stringBuilder.toString())
      cacheArtifacts(stringBuilder.toString)
      cache.get((scalaVersion, hydraVersion))
    } else {
      artifacts
    }
  }
}
