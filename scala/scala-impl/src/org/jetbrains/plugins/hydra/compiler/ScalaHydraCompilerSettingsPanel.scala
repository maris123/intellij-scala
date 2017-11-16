package org.jetbrains.plugins.hydra.compiler

import com.intellij.openapi.project.Project

/**
  * @author Maris Alexandru
  */
class ScalaHydraCompilerSettingsPanel(project: Project) extends HydraCompilerSettingsPanel {

  noOfCoresComboBox.setItems(Array.range(1, Runtime.getRuntime.availableProcessors() + 1).map(_.toString).sortWith(_ > _))
  sourcePartitionerComboBox.setItems(SourcePartitioner.values.map(_.value).toArray)

  def getState: HydraCompilerSettings = {
    val state = new HydraCompilerSettings()

    state.noOfCores = selectedNoOfCores
    state.sourcePartitioner = selectedSourcePartitioner

    state
  }

  def setState(state: HydraCompilerSettings): Unit = {
    setSelectedNoOfCores(state.noOfCores)
    setSelectedSourcePartitioner(state.sourcePartitioner)
  }

  def saveTo(profile: HydraCompilerSettingsProfile): Unit = {
    profile.setSettings(getState)
  }

  def setProfile(profile: HydraCompilerSettingsProfile): Unit = {
    setState(profile.getSettings)
  }

  def selectedNoOfCores: String = noOfCoresComboBox.getSelectedItem.toString

  def setSelectedNoOfCores(numberOfCores: String): Unit = noOfCoresComboBox.setSelectedItem(numberOfCores)

  def selectedSourcePartitioner: String = sourcePartitionerComboBox.getSelectedItem.toString

  def setSelectedSourcePartitioner(sourcePartitioner: String): Unit = sourcePartitionerComboBox.setSelectedItem(sourcePartitioner)

}
