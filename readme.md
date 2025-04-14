# PR Jumper

PR Jumper is an IntelliJ plugin that helps you navigate unviewed files in a GitHub Pull Request, right from your IDE.

### ? Features

- Fetches PR metadata via GitHub API
- Lets you jump between files changed in a PR using `Ctrl + 5`
- PR Mode status widget in the IntelliJ status bar
- GitHub token stored securely (or provided via env var)
- Supports PR URLs or plain numbers (auto-detects repo from `.git/config`)

---

## ? Setup

### 1. Install the Plugin

Clone this repo and run the plugin via **IntelliJ Plugin DevKit**.

### 2. Provide a GitHub Access Token

You need a [GitHub personal access token](https://github.com/settings/tokens?type=beta) with:

- `repo` scope (for private repos)
- `read:org` if needed

You can set the token either:

#### ? Automatically via `GITHUB_TOKEN` environment variable:

- On **macOS/Linux**:
  ```bash
  echo 'export GITHUB_TOKEN=ghp_yourtokenhere' >> ~/.zshrc
  source ~/.zshrc
  ```

- On **Windows (PowerShell)**:
  ```powershell
  [Environment]::SetEnvironmentVariable("GITHUB_TOKEN", "ghp_yourtokenhere", "User")
  ```

Restart IntelliJ after setting the variable.

#### ? Or enter it manually when prompted on plugin launch.

---

## ?? Usage

- Click **PR Mode** under the `Tools` menu
- Enter a PR ID (`42`) or full PR URL (`https://github.com/owner/repo/pull/42`)
- Jump between unviewed files with **Ctrl + 5**
- View active PR in the status bar

---

## ? Development

This plugin uses:

- Kotlin + Gradle
- IntelliJ Platform 2024.x
- OkHttp for GitHub API
- org.json for response parsing

---

## ? License

MIT — feel free to fork, extend, and PR back!