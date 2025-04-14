package com.github.tomdesch.prjumper.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class PRModeAction : AnAction("PR Mode") {

    override fun actionPerformed(e: AnActionEvent) {
        val prInput = Messages.showInputDialog(
            e.project,
            "Enter GitHub PR ID or URL:",
            "Activate PR Mode",
            Messages.getQuestionIcon()
        )

        if (!prInput.isNullOrBlank()) {
            // TODO: Parse and store PR info
            Messages.showInfoMessage(e.project, "PR Mode activated for: $prInput", "PR Mode")
        }
    }
}