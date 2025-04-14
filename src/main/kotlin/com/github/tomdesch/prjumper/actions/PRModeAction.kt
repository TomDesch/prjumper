package com.github.tomdesch.prjumper.actions

import com.github.tomdesch.prjumper.github.GitHubPRFetcher
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

data class PullRequestRef(val owner: String, val repo: String, val number: Int)

object PRContext {
    var current: PullRequestRef? = null
    var unviewedFiles: MutableList<String> = mutableListOf()

    fun reset() {
        current = null
        unviewedFiles.clear()
    }
}


class PRModeAction : AnAction() {

    init {
        templatePresentation.text = "PR Mode"
        templatePresentation.description = "Activate GitHub PR navigation mode"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Step 1: Select repo
        val repos = GitHubPRFetcher.fetchUserRepos()
        if (repos.isEmpty()) {
            Messages.showErrorDialog(project, "No accessible GitHub repositories found.", "PR Mode")
            return
        }

        val repoOptions = repos.map { "${it.owner}/${it.name}" }.toTypedArray()
        val selectedRepo = Messages.showEditableChooseDialog(
            "Select a repository:",
            "PR Mode",
            Messages.getQuestionIcon(),
            repoOptions,
            repoOptions.firstOrNull(),
            null
        ) ?: return

        val (owner, repo) = selectedRepo.split("/")

        // Step 2: Select PR
        val prList = GitHubPRFetcher.fetchOpenPRs(owner, repo)
        if (prList.isEmpty()) {
            Messages.showErrorDialog(project, "No open PRs found or API call failed.", "PR Mode")
            return
        }

        val displayOptions = prList.map { "#${it.number} - ${it.title}" }.toTypedArray()
        val selected = Messages.showEditableChooseDialog(
            "Select a pull request to jump through:",
            "PR Mode",
            Messages.getQuestionIcon(),
            displayOptions,
            displayOptions.firstOrNull(),
            null
        ) ?: return

        val selectedNumber = selected.substringAfter("#").substringBefore(" ").toIntOrNull()
        val selectedPR = prList.find { it.number == selectedNumber } ?: return

        // Step 3: Load PR into context
        val ref = PullRequestRef(owner, repo, selectedPR.number)
        PRContext.current = ref

        Messages.showInfoMessage(project, "PR Mode activated for ${ref.owner}/${ref.repo}#${ref.number}", "PR Mode")

        val files = GitHubPRFetcher.fetchChangedFiles()
        if (files.isEmpty()) {
            Messages.showErrorDialog(project, "No files found in PR or API call failed.", "PR Mode")
            return
        }

        PRContext.unviewedFiles.clear()
        PRContext.unviewedFiles.addAll(files)
    }
}