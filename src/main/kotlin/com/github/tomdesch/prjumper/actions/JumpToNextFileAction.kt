package com.github.tomdesch.prjumper.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class JumpToNextFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (PRContext.unviewedFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No more unviewed files.", "PR Mode")
            return
        }

        val next = PRContext.unviewedFiles.removeAt(0)

        // Search file in local file system
        val file = findFileInProject(project, next)
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true)
        } else {
            Messages.showErrorDialog(project, "File not found in project: $next", "PR Mode")
        }
    }

    private fun findFileInProject(project: com.intellij.openapi.project.Project, path: String): VirtualFile? {
        val base = project.basePath ?: return null
        val file = File(base, path)
        return LocalFileSystem.getInstance().findFileByIoFile(file)
    }
}