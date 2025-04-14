package com.github.tomdesch.prjumper.startup

import com.github.tomdesch.prjumper.auth.GitHubTokenService
import com.github.tomdesch.prjumper.ui.GitHubTokenDialog
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.EventQueue

@Service(Service.Level.PROJECT)
class GitHubTokenStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val storedToken = GitHubTokenService.getInstance().getToken()

        if (storedToken.isNullOrBlank() || !isTokenValid(storedToken)) {
            withContext(Dispatchers.Main) {
                EventQueue.invokeLater {
                    GitHubTokenDialog().show()
                }
            }
        }
    }

    private fun isTokenValid(token: String): Boolean {
        val request = Request.Builder()
            .url("https://api.github.com/user")
            .header("Authorization", "token $token")
            .build()

        return try {
            OkHttpClient().newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }
}