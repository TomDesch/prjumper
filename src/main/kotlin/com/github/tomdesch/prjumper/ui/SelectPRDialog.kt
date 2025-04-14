package com.github.tomdesch.prjumper.ui

import com.github.tomdesch.prjumper.actions.PullRequestRef
import com.github.tomdesch.prjumper.github.GitHubPRFetcher
import com.github.tomdesch.prjumper.github.GitHubPRFetcher.PullRequestSummary
import com.github.tomdesch.prjumper.github.GitHubPRFetcher.RepoSummary
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import java.awt.event.ItemEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SelectPRDialog : DialogWrapper(true) {

    private val repoDropdown = ComboBox<String>().apply { isEditable = true }
    private val prDropdown = ComboBox<String>().apply { isEditable = true }

    private val repoMap = mutableMapOf<String, RepoSummary>()
    private val prMap = mutableMapOf<String, PullRequestSummary>()

    private val allRepoKeys = mutableListOf<String>()
    private val allPRKeys = mutableListOf<String>()

    var selectedPR: PullRequestRef? = null
        private set

    init {
        title = "Select PR"
        init()
        loadRepos()
        setupFiltering(repoDropdown, allRepoKeys)
        setupFiltering(prDropdown, allPRKeys)
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        panel.add(JBLabel("Select Repository:"))
        panel.add(repoDropdown)
        panel.add(Box.createVerticalStrut(8))
        panel.add(JBLabel("Select Pull Request:"))
        panel.add(prDropdown)

        repoDropdown.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                val selected = it.item?.toString() ?: return@addItemListener
                if (repoMap.containsKey(selected)) {
                    loadPRsFor(selected)
                }
            }
        }

        return panel
    }

    override fun doOKAction() {
        val repoKey = repoDropdown.selectedItem?.toString() ?: return
        val prKey = prDropdown.selectedItem?.toString() ?: return

        val repo = repoMap[repoKey] ?: return
        val pr = prMap[prKey] ?: return

        selectedPR = PullRequestRef(repo.owner, repo.name, pr.number)
        super.doOKAction()
    }

    private fun loadRepos() {
        val repos = GitHubPRFetcher.fetchUserRepos()
        allRepoKeys.clear()
        repoMap.clear()
        (repoDropdown.model as DefaultComboBoxModel).removeAllElements()

        repos.forEach {
            val key = "${it.owner}/${it.name}"
            allRepoKeys.add(key)
            repoMap[key] = it
        }

        allRepoKeys.forEach { (repoDropdown.model as DefaultComboBoxModel).addElement(it) }

        if (allRepoKeys.isNotEmpty()) {
            repoDropdown.selectedItem = allRepoKeys.first()
            loadPRsFor(allRepoKeys.first())
        }
    }

    private fun loadPRsFor(repoKey: String) {
        val (owner, name) = repoKey.split("/")
        val prs = GitHubPRFetcher.fetchOpenPRs(owner, name)

        allPRKeys.clear()
        prMap.clear()
        (prDropdown.model as DefaultComboBoxModel).removeAllElements()

        prs.forEach {
            val label = "#${it.number} - ${it.title}"
            allPRKeys.add(label)
            prMap[label] = it
        }

        allPRKeys.forEach { (prDropdown.model as DefaultComboBoxModel).addElement(it) }
        if (allPRKeys.isNotEmpty()) {
            prDropdown.selectedItem = allPRKeys.first()
        }
    }

    private fun setupFiltering(dropdown: ComboBox<String>, allItems: List<String>) {
        val editor = dropdown.editor.editorComponent
        if (editor is JTextField) {
            editor.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = filter()
                override fun removeUpdate(e: DocumentEvent?) = filter()
                override fun changedUpdate(e: DocumentEvent?) = filter()

                private fun filter() {
                    val input = editor.text
                    val model = dropdown.model as DefaultComboBoxModel<String>
                    model.removeAllElements()
                    allItems.filter { it.contains(input, ignoreCase = true) }.forEach {
                        model.addElement(it)
                    }
                    dropdown.showPopup()
                }
            })
        }
    }
}