package org.jetbrains.plugins.scala.compiler

import java.nio.file.Paths

import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.compiler.SourcePartitioner.Auto

/**
  * @author Maris Alexandru
  */
class HydraCompilerSettings(project: Project) {

  var noOfCores: String = Math.ceil(Runtime.getRuntime.availableProcessors()/2D).toInt.toString

  var hydraStorePath: String = getDefaultHydraStorePath

  var sourcePartitioner: String = Auto.value

  private def getDefaultHydraStorePath = Paths.get(project.getBaseDir.getPresentableUrl, ".hydra").toString
}

object SourcePartitioner {
  sealed abstract class SourcePartitioner(val value: String)

  case object Auto extends SourcePartitioner("auto")
  case object Explicit extends SourcePartitioner("explicit")
  case object Plain extends SourcePartitioner("plain")
  case object Package extends SourcePartitioner("package")

  val values: Seq[SourcePartitioner] = Seq(Auto, Explicit, Plain, Package)
}

