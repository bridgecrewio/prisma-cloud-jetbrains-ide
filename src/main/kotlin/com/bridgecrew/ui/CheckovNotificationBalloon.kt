package com.bridgecrew.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object CheckovNotificationBalloon {

    private const val GROUP_ID = "CheckovError"
    private val NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(GROUP_ID)
    fun showNotification(project: Project, notificationContent: String, notificationType: NotificationType) {
        val notification = NOTIFICATION_GROUP.createNotification(
                notificationContent,
                notificationType
        )
        notification.notify(project)
    }
}