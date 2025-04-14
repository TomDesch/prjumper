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
        val input = Messages.showInputDialog(
            e.project, "Enter GitHub PR ID or URL:", "Activate PR Mode", Messages.getQuestionIcon()
        ) ?: return

        val ref = parsePRInput(input)
        if (ref != null) {
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

        } else {
            Messages.showErrorDialog(e.project, "Invalid PR input", "PR Mode")
        }
    }

    private fun parsePRInput(input: String): PullRequestRef? {
        val urlPattern = Regex("""github\.com/([^/]+)/([^/]+)/pull/(\d+)""")
        val match = urlPattern.find(input)
        if (match != null) {
            val (owner, repo, number) = match.destructured
            return PullRequestRef(owner, repo, number.toInt())
        }

        val prNumber = input.toIntOrNull()
        if (prNumber != null) {
            val (owner, repo) = detectRepoFromGit() ?: return null
            return PullRequestRef(owner, repo, prNumber)
        }

        return null
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