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

    private val repoDropdown = ComboBox<String>(DefaultComboBoxModel()).apply { isEditable = true }
    private val prDropdown = ComboBox<String>(DefaultComboBoxModel()).apply { isEditable = true }

    private val repoMap = mutableMapOf<String, RepoSummary>()
    private val prMap = mutableMapOf<String, PullRequestSummary>()
    private val allRepos = mutableListOf<String>()
    private val allPRs = mutableListOf<String>()

    var selectedPR: PullRequestRef? = null
        private set

    init {
        title = "Select PR"
        loadRepos()
        setupFiltering(repoDropdown, allRepos) { filterRepos(it) }
        setupFiltering(prDropdown, allPRs) { filterPRs(it) }
        init()
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
                loadPRsFor(it.item.toString())
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
        val model = repoDropdown.model as DefaultComboBoxModel<String>
        model.removeAllElements()
        allRepos.clear()
        repoMap.clear()

        repos.forEach {
            val key = "${it.owner}/${it.name}"
            allRepos.add(key)
            repoMap[key] = it
        }

        allRepos.forEach { model.addElement(it) }

        if (allRepos.isNotEmpty()) {
            repoDropdown.selectedItem = allRepos.first()
            loadPRsFor(allRepos.first())
        }
    }

    private fun loadPRsFor(repoKey: String) {
        prDropdown.removeAllItems()
        allPRs.clear()
        prMap.clear()

        val (owner, name) = repoKey.split("/")
        val prs = GitHubPRFetcher.fetchOpenPRs(owner, name)
        val model = prDropdown.model as DefaultComboBoxModel<String>

        prs.forEach {
            val label = "#${it.number} - ${it.title}"
            allPRs.add(label)
            prMap[label] = it
        }

        allPRs.forEach { model.addElement(it) }
    }

    private fun setupFiltering(dropdown: ComboBox<String>, sourceList: List<String>, onFilter: (String) -> Unit) {
        val editor = dropdown.editor.editorComponent
        if (editor is JTextField) {
            editor.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = onFilter(editor.text)
                override fun removeUpdate(e: DocumentEvent?) = onFilter(editor.text)
                override fun changedUpdate(e: DocumentEvent?) = onFilter(editor.text)
            })
        }
    }

    private fun filterRepos(text: String) {
        val model = repoDropdown.model as DefaultComboBoxModel<String>
        model.removeAllElements()
        allRepos.filter { it.contains(text, ignoreCase = true) }.forEach { model.addElement(it) }
        repoDropdown.showPopup()
    }

    private fun filterPRs(text: String) {
        val model = prDropdown.model as DefaultComboBoxModel<String>
        model.removeAllElements()
        allPRs.filter { it.contains(text, ignoreCase = true) }.forEach { model.addElement(it) }
        prDropdown.showPopup()
    }
}