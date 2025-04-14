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

            PRContext.unviewedFiles.clear()
            PRContext.unviewedFiles.addAll(files)

        } else {
            Messages.showErrorDialog(e.project, "Invalid PR input", "PR Mode")
        }
    }

    private fun parsePRInput(input: String): PullRequestRef? {
        val urlPattern = Regex("""github\.com/([^/]+)/([^/]+)/pull/(\d+)""")
        val match = urlPattern.find(input)
        return when {
            match != null -> {
                val (owner, repo, number) = match.destructured
                PullRequestRef(owner, repo, number.toInt())
            }

            input.toIntOrNull() != null -> {
                // Default repo fallback for plain number (optional: prompt for owner/repo if desired)
                PullRequestRef("your-owner", "your-repo", input.toInt())
            }

            else -> null
        }
    }
}