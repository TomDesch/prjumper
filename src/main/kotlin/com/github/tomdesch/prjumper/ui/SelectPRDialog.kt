package com.github.tomdesch.prjumper.ui

import com.github.tomdesch.prjumper.actions.PullRequestRef
import com.github.tomdesch.prjumper.github.GitHubPRFetcher
import com.github.tomdesch.prjumper.github.GitHubPRFetcher.PullRequestSummary
import com.github.tomdesch.prjumper.github.GitHubPRFetcher.RepoSummary
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

class SelectPRDialog : DialogWrapper(true) {

    private val repoMap = mutableMapOf<String, RepoSummary>()
    private val prMap = mutableMapOf<String, PullRequestSummary>()

    private val allRepoKeys = mutableListOf<String>()
    private val allPRKeys = mutableListOf<String>()

    private var selectedRepoKey: String? = null
    private var selectedPRKey: String? = null

    private val repoButton = JButton("Select Repository").apply {
        alignmentX = Component.LEFT_ALIGNMENT
        preferredSize = Dimension(350, 30)
        addActionListener {
            val dialog = SearchableListDialog("Select Repository", allRepoKeys)
            if (dialog.showAndGet()) {
                selectedRepoKey = dialog.selectedItem
                text = selectedRepoKey ?: "Select Repository"
                selectedPRKey = null
                prButton.text = "Select Pull Request"
                if (selectedRepoKey != null) {
                    loadPRsFor(selectedRepoKey!!)
                }
            }
        }
    }

    private val prButton = JButton("Select Pull Request").apply {
        alignmentX = Component.LEFT_ALIGNMENT
        preferredSize = Dimension(350, 30)
        addActionListener {
            val dialog = SearchableListDialog("Select Pull Request", allPRKeys)
            if (dialog.showAndGet()) {
                selectedPRKey = dialog.selectedItem
                text = selectedPRKey ?: "Select Pull Request"
            }
        }
    }

    var selectedPR: PullRequestRef? = null
        private set

    init {
        title = "Select PR"
        init()
        loadRepos()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        panel.add(JBLabel("Repository:"))
        panel.add(repoButton)
        panel.add(Box.createVerticalStrut(12))
        panel.add(JBLabel("Pull Request:"))
        panel.add(prButton)

        return panel
    }

    override fun doOKAction() {
        val repoKey = selectedRepoKey ?: return
        val prKey = selectedPRKey ?: return

        val repo = repoMap[repoKey] ?: return
        val pr = prMap[prKey] ?: return

        selectedPR = PullRequestRef(repo.owner, repo.name, pr.number)
        super.doOKAction()
    }

    private fun loadRepos() {
        val repos = GitHubPRFetcher.fetchUserRepos()
        allRepoKeys.clear()
        repoMap.clear()

        repos.forEach {
            val key = "${it.owner}/${it.name}"
            allRepoKeys.add(key)
            repoMap[key] = it
        }
    }

    private fun loadPRsFor(repoKey: String) {
        val (owner, name) = repoKey.split("/")
        val prs = GitHubPRFetcher.fetchOpenPRs(owner, name)

        allPRKeys.clear()
        prMap.clear()

        prs.forEach {
            val label = "#${it.number} - ${it.title}"
            allPRKeys.add(label)
            prMap[label] = it
        }
    }
}