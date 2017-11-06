package org.jetbrains.plugins.hydra.compiler

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import java.nio.file.Paths
import com.intellij.openapi.project.Project
import javax.swing.event.DocumentEvent
import com.intellij.ui.DocumentAdapter
import com.intellij.openapi.ui.TextComponentAccessor

/**
  * @author Maris Alexandru
  */
class ScalaHydraCompilerSettingsPanel (project: Project) extends HydraCompilerSettingsPanel {

  private val fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false)

  noOfCoresComboBox.setItems(Array.range(1, Runtime.getRuntime.availableProcessors() + 1).map(_.toString).sortWith(_ > _))
  sourcePartitionerComboBox.setItems(SourcePartitioner.values.map(_.value).toArray)

  hydraStoreDirectoryField.addBrowseFolderListener("", "Hydra Store Path", null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT)

  hydraStoreDirectoryField.getTextField.getDocument.addDocumentListener(new DocumentAdapter() {
    override protected def textChanged(e: DocumentEvent): Unit = {
      hydraStoreDirectoryField.getTextField.setForeground(if (hydraStoreDirectoryField.getText == Paths.get(project.getBaseDir.getPresentableUrl).toString) getDefaultValueColor
      else getChangedValueColor)
    }
  })

  def getState: HydraCompilerSettings = {
    val state = new HydraCompilerSettings(project)

    state.noOfCores = selectedNoOfCores
    state.sourcePartitioner = selectedNoOfCores
    state.hydraStorePath = getHydraStoreDirectory

    state
  }

  def setState(state: HydraCompilerSettings): Unit = {
    setSelectedNoOfCores(state.noOfCores)
    setSelectedSourcePartitioner(state.sourcePartitioner)
    setHydraStoreDirectory(state.hydraStorePath)
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

  def getHydraStoreDirectory: String = hydraStoreDirectoryField.getText

  def setHydraStoreDirectory(path: String): Unit = hydraStoreDirectoryField.setText(path)
}