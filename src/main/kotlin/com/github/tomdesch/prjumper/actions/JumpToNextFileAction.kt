package com.github.tomdesch.prjumper.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class JumpToNextFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return


        if (PRContext.unviewedFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No more unviewed files.", "PR Mode Ended")
            PRContext.reset()
            return
        }

        val remaining = PRContext.unviewedFiles.size
        val next = PRContext.unviewedFiles.removeAt(0)
        val total = remaining + 1 // current file + remaining
        val current = total - remaining

        Messages.showInfoMessage(project, "Jumped to file $current of $total:\n$next", "PR Jumper")
        val file = findFileAnywhere(project, next)
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true)
        } else {
            Messages.showErrorDialog(project, "File not found: $next", "PR Mode")
        }

        // If that was the last file
        if (PRContext.unviewedFiles.isEmpty()) {
            Messages.showInfoMessage(project, "You’ve viewed all files in the PR.", "PR Mode Complete")
            PRContext.reset()
        }
    }

    private fun findFileAnywhere(project: Project, path: String): VirtualFile? {
        val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
        for (root in sourceRoots) {
            val candidate = root.findFileByRelativePath(path.substringAfter("src/main/java/"))
            if (candidate != null) return candidate
        }

        // Fallback: try absolute path under project
        val basePath = project.basePath ?: return null
        val absoluteFile = File(basePath, path)
        return LocalFileSystem.getInstance().findFileByIoFile(absoluteFile)
    }

}