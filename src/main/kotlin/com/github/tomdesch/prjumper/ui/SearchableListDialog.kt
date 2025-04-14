package com.github.tomdesch.prjumper.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchableListDialog(
    private val titleText: String,
    private val allItems: List<String>
) : DialogWrapper(true) {

    private val listModel = DefaultListModel<String>()
    private val list = JBList(listModel)
    private val searchField = JTextField()

    var selectedItem: String? = null
        private set

    init {
        title = titleText
        listModel.addAll(allItems)
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(searchField, BorderLayout.NORTH)
        panel.add(JBScrollPane(list), BorderLayout.CENTER)

        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.addListSelectionListener {
            selectedItem = list.selectedValue
        }

        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = filter()
            override fun removeUpdate(e: DocumentEvent?) = filter()
            override fun changedUpdate(e: DocumentEvent?) = filter()

            private fun filter() {
                val text = searchField.text.lowercase()
                listModel.clear()
                allItems.filter { it.contains(text, ignoreCase = true) }.forEach { listModel.addElement(it) }
                if (listModel.size > 0) list.selectedIndex = 0
            }
        })

        return panel
    }

    override fun doOKAction() {
        selectedItem = list.selectedValue
        super.doOKAction()
    }
}