package com.github.tomdesch.prjumper.ui

import com.github.tomdesch.prjumper.actions.PullRequestRef
import com.github.tomdesch.prjumper.github.GitHubPRFetcher
import com.github.tomdesch.prjumper.github.GitHubPRFetcher.PullRequestSummary
import com.github.tomdesch.prjumper.github.GitHubPRFetcher.RepoSummary
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import java.awt.event.ItemEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class SelectPRDialog : DialogWrapper(true) {

    private val repoDropdown = ComboBox<String>()
    private val prDropdown = ComboBox<String>()

    private val repoMap = mutableMapOf<String, RepoSummary>()
    private val prMap = mutableMapOf<String, PullRequestSummary>()

    var selectedPR: PullRequestRef? = null
        private set

    init {
        title = "Select PR"
        loadRepos()
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
        repos.forEach {
            val key = "${it.owner}/${it.name}"
            repoDropdown.addItem(key)
            repoMap[key] = it
        }

        if (repos.isNotEmpty()) {
            loadPRsFor("${repos.first().owner}/${repos.first().name}")
        }
    }

    private fun loadPRsFor(repoKey: String) {
        prDropdown.removeAllItems()
        prMap.clear()

        val (owner, name) = repoKey.split("/")
        val prs = GitHubPRFetcher.fetchOpenPRs(owner, name)
        prs.forEach {
            val label = "#${it.number} - ${it.title}"
            prDropdown.addItem(label)
            prMap[label] = it
        }
    }
}