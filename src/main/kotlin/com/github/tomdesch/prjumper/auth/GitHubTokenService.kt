package com.github.tomdesch.prjumper.auth

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@State(name = "GitHubTokenService", storages = [Storage("prjumper.xml")])
class GitHubTokenService : PersistentStateComponent<GitHubTokenService.State> {

    class State {
        var token: String? = null
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun getToken(): String? = state.token

    fun setToken(token: String) {
        state.token = token
    }

    companion object {
        fun getInstance(): GitHubTokenService = service()
    }
}