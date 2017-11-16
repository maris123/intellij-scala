package org.jetbrains.plugins.hydra.compiler

import org.jetbrains.plugins.hydra.compiler.SourcePartitioner.Auto

import scala.beans.BeanProperty

/**
  * @author Maris Alexandru
  */
class HydraCompilerSettings(state: HydraCompilerSettingsState) {
  def this() {
    this(new HydraCompilerSettingsState())
  }

  loadState(state)

  var noOfCores: String = Math.ceil(Runtime.getRuntime.availableProcessors()/2D).toInt.toString

  var sourcePartitioner: String = Auto.value

  def loadState(state: HydraCompilerSettingsState): Unit = {
    noOfCores = state.getNoOfCores
    sourcePartitioner = state.getSourcePartitioner
  }

  def getState: HydraCompilerSettingsState = {
    val state = new HydraCompilerSettingsState()

    state.setNoOfCores(noOfCores)
    state.setSourcePartitioner(sourcePartitioner)

    state
  }
}

class HydraCompilerSettingsState {
  @BeanProperty
  var noOfCores: String = ""

  @BeanProperty
  var sourcePartitioner: String = ""

  def canEqual(other: Any): Boolean = other.isInstanceOf[HydraCompilerSettingsState]

  override def equals(other: Any): Boolean = other match {
    case that: HydraCompilerSettingsState =>
      (that canEqual this) &&
        noOfCores == that.noOfCores &&
        sourcePartitioner == that.sourcePartitioner
    case _ => false
  }
}

object SourcePartitioner {
  sealed abstract class SourcePartitioner(val value: String)

  case object Auto extends SourcePartitioner("auto")
  case object Explicit extends SourcePartitioner("explicit")
  case object Plain extends SourcePartitioner("plain")
  case object Package extends SourcePartitioner("package")

  val values: Seq[SourcePartitioner] = Seq(Auto, Explicit, Plain, Package)
}

