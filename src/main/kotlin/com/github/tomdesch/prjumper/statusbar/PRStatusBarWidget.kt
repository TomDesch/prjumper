package com.github.tomdesch.prjumper.statusbar

import com.github.tomdesch.prjumper.actions.PRContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class PRStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "PRJumperStatusWidget"

    override fun getDisplayName(): String = "PR Jumper"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget =
        object : StatusBarWidget, StatusBarWidget.TextPresentation {
            private var statusBar: StatusBar? = null

            override fun ID(): String = "PRJumperStatusWidget"

            override fun install(statusBar: StatusBar) {
                this.statusBar = statusBar
            }

            override fun dispose() {}

            override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

            override fun getText(): String {
                val current = PRContext.current
                return if (current != null) {
                    "? PR Mode: ${current.owner}/${current.repo}#${current.number}"
                } else {
                    ""
                }
            }

            override fun getTooltipText(): String = "Current PR Mode"
            override fun getAlignment(): Float = 0.5f
            override fun getClickConsumer(): Consumer<MouseEvent>? = null

        }

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}