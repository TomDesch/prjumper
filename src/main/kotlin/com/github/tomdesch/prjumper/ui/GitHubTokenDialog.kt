package com.github.tomdesch.prjumper.ui

import com.github.tomdesch.prjumper.auth.GitHubTokenService
import com.intellij.openapi.ui.DialogWrapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Component.LEFT_ALIGNMENT
import java.awt.Desktop
import java.net.URI
import javax.swing.*


class GitHubTokenDialog : DialogWrapper(true) {

    private val tokenField = JPasswordField()

    init {
        title = "GitHub Access Token"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val infoLabel = JLabel("Enter your GitHub personal access token:")
        panel.add(infoLabel)

        tokenField.columns = 40
        panel.add(tokenField)

        val linkButton = JButton("Generate token...")
        linkButton.alignmentX = LEFT_ALIGNMENT
        linkButton.addActionListener {
            Desktop.getDesktop().browse(URI("https://github.com/settings/tokens?type=beta"))
        }
        panel.add(Box.createVerticalStrut(10))
        panel.add(linkButton)

        return panel
    }

    override fun doOKAction() {
        val token = String(tokenField.password).trim()
        if (token.isBlank()) {
            JOptionPane.showMessageDialog(null, "Token cannot be empty.")
            return
        }

        val request = Request.Builder()
            .url("https://api.github.com/user")
            .header("Authorization", "token $token")
            .build()

        val isValid = try {
            OkHttpClient().newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }

        if (!isValid) {
            JOptionPane.showMessageDialog(null, "Invalid GitHub token.")
            return
        }

        // Save token to persistent service
        GitHubTokenService.getInstance().setToken(token)

        super.doOKAction()
    }

}