package com.github.tomdesch.prjumper.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

class JumpToNextFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (PRContext.unviewedFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No more unviewed files.", "PR Mode Ended")
            PRContext.reset()
            return
        }

        val next = PRContext.unviewedFiles.removeAt(0)
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
        // Try local disk first
        val basePath = project.basePath ?: return null
        val localFile = File(basePath, path)
        val found = LocalFileSystem.getInstance().findFileByIoFile(localFile)
        if (found != null) return found

        // Try GitHub plugin virtual files
        val githubRoot = VirtualFileManager.getInstance().findFileByUrl("github://") ?: return null
        var match: VirtualFile? = null

        VfsUtilCore.iterateChildrenRecursively(githubRoot, null) { file ->
            if (file.path.endsWith(path)) {
                match = file
                false // stop iteration
            } else {
                true // keep going
            }
        }

        return match
    }
}