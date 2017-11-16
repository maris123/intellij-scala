package org.jetbrains.plugins.hydra.compiler

import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

/**
  * @author Maris Alexandru
  */
object HydraCompilerSettingsManager {

  private val HydraLogKey = "hydra.logFile"

  def showHydraCompileSettingsDialog(project: Project): Unit = ShowSettingsUtil.getInstance().showSettingsDialog(project, "Hydra Compiler")

  def getHydraLogJvmParameter(project: Project): String = {
    val settings = HydraCompilerConfiguration.getInstance(project)
    if (settings.isHydraEnabled)
      s"-Dhydra.logFile=${settings.hydraLogLocation}"
    else
      ""
  }

  def setHydraLogSystemProperty(project: Project): Unit = {
    if (System.getProperty(HydraLogKey) == null) {
      val settings = HydraCompilerConfiguration.getInstance(project)
      System.setProperty(HydraLogKey, settings.hydraLogLocation)
    }
  }
}
