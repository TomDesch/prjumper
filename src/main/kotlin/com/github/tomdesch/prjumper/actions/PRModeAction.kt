package com.github.tomdesch.prjumper.actions

import com.github.tomdesch.prjumper.github.GitHubPRFetcher
import com.github.tomdesch.prjumper.ui.SelectPRDialog
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

        val dialog = SelectPRDialog()
        if (!dialog.showAndGet()) return

        val ref = dialog.selectedPR ?: return
        PRContext.current = ref

        Messages.showInfoMessage(
            project, "PR Mode activated for ${ref.owner}/${ref.repo}#${ref.number}", "PR Mode"
        )

        val files = GitHubPRFetcher.fetchChangedFiles()
        if (files.isEmpty()) {
            Messages.showErrorDialog(project, "No files found in PR or API call failed.", "PR Mode")
            return
        }

        PRContext.unviewedFiles.clear()
        PRContext.unviewedFiles.addAll(files)
    }
}