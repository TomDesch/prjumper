package com.github.tomdesch.prjumper.actions

import com.github.tomdesch.prjumper.github.GitHubPRFetcher
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.File

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
        val (owner, repo) = detectRepoFromGit() ?: run {
            Messages.showErrorDialog(e.project, "Could not detect GitHub repo from .git/config", "PR Mode")
            return
        }

        val prList = GitHubPRFetcher.fetchOpenPRs(owner, repo)
        if (prList.isEmpty()) {
            Messages.showErrorDialog(e.project, "No open PRs found or API call failed.", "PR Mode")
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

        val ref = PullRequestRef(owner, repo, selectedPR.number)

        PRContext.current = ref
        Messages.showInfoMessage(
            e.project, "PR Mode activated for ${ref.owner}/${ref.repo}#${ref.number}", "PR Mode"
        )

        val files = GitHubPRFetcher.fetchChangedFiles()
        if (files.isEmpty()) {
            Messages.showErrorDialog(e.project, "No files found in PR or API call failed.", "PR Mode")
            return
        }

        PRContext.unviewedFiles.addAll(files)

    }

    private fun detectRepoFromGit(): Pair<String, String>? {
        val gitConfig = File(".git/config")
        if (!gitConfig.exists()) return null

        val lines = gitConfig.readLines()
        val urlLine = lines.firstOrNull { it.trim().startsWith("url =") } ?: return null

        val url = urlLine.substringAfter("=").trim()
        val regex = Regex("""github\.com[:/](.*?)/(.*?)(\.git)?$""")
        val match = regex.find(url) ?: return null

        val (owner, repo) = match.destructured
        return owner to repo.removeSuffix(".git")
    }

}