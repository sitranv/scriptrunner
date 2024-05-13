package com.mgm_tp.atlas.jira.udc.helper

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventType
import com.atlassian.jira.issue.security.IssueSecurityLevelManager
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager
import com.atlassian.jira.notification.NotificationSchemeManager
import com.atlassian.jira.notification.type.NotificationType
import com.atlassian.jira.permission.JiraPermissionHolderType
import com.atlassian.jira.permission.PermissionSchemeManager
import com.atlassian.jira.permission.ProjectPermissions
import com.atlassian.jira.scheme.SchemeEntity
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import org.apache.log4j.Logger

class SchemeHelper {
    Logger log

    private PermissionSchemeManager permissionSchemeManager
    private NotificationSchemeManager notificationSchemeManager
    private IssueSecuritySchemeManager issueSecuritySchemeManager
    private IssueSecurityLevelManager issueSecurityLevelManager
    private UserManager userManager

    SchemeHelper(Logger log) {
        this.log = log
        this.permissionSchemeManager = ComponentAccessor.permissionSchemeManager
        this.notificationSchemeManager = ComponentAccessor.notificationSchemeManager
        this.issueSecuritySchemeManager = ComponentAccessor.getComponent(IssueSecuritySchemeManager)
        this.issueSecurityLevelManager = ComponentAccessor.issueSecurityLevelManager
        this.userManager = ComponentAccessor.userManager
    }

    def createPermissionScheme(ApplicationUser user) {
        def schemeName = "Generated Permission Scheme"
        def nameExists = { String name -> permissionSchemeManager.getSchemeObjects().any { it.name == name } }
        def uniqueName = ContextHelper.createUniqueName(schemeName, nameExists)
        def createdScheme = permissionSchemeManager.createSchemeObject(uniqueName, "")

        def schemeEntity = new SchemeEntity(JiraPermissionHolderType.USER.key, user.key, ProjectPermissions.ADMINISTER_PROJECTS)
        def schemeGV = permissionSchemeManager.getScheme(createdScheme.id)
        permissionSchemeManager.createSchemeEntity(schemeGV, schemeEntity)
    }

    def addPermissionScheme(String schemeName, ApplicationUser user) {
        def scheme = permissionSchemeManager.getSchemeObjects().find { it.name == schemeName }
        def schemeGV = permissionSchemeManager.getScheme(scheme.id)
        def schemeEntity = new SchemeEntity(JiraPermissionHolderType.USER.key, user.key, ProjectPermissions.ADMINISTER_PROJECTS)

        permissionSchemeManager.createSchemeEntity(schemeGV, schemeEntity)
    }

    def createNotificationScheme(ApplicationUser user) {
        def schemeName = "Generated Notification Scheme"
        def nameExists = { String name -> notificationSchemeManager.getSchemeObjects().any { it.name == name } }
        def schemeEntity = new SchemeEntity(NotificationType.SINGLE_USER.dbCode(), user.key, EventType.ISSUE_GENERICEVENT_ID)
        def uniqueName = ContextHelper.createUniqueName(schemeName, nameExists)

        def createdScheme = notificationSchemeManager.createSchemeObject(uniqueName, "")
        def schemeGV = notificationSchemeManager.getScheme(createdScheme.id)
        notificationSchemeManager.createSchemeEntity(schemeGV, schemeEntity)
    }

    def addNotificationScheme(String schemeName, ApplicationUser user) {
        def scheme = notificationSchemeManager.getSchemeObjects().find { it.name == schemeName }
        def schemeGV = notificationSchemeManager.getScheme(scheme.id)
        def schemeEntity = new SchemeEntity(NotificationType.SINGLE_USER.dbCode(), user.key, EventType.ISSUE_GENERICEVENT_ID)

        notificationSchemeManager.createSchemeEntity(schemeGV, schemeEntity)
    }

    def createIssueSecurityScheme(ApplicationUser user) {
        def schemeName = "Generated Issue Security Scheme"
        def nameExists = { String name -> issueSecuritySchemeManager.getSchemeObjects().any { it.name == name } }
        def uniqueName = ContextHelper.createUniqueName(schemeName, nameExists)
        def createdScheme = issueSecuritySchemeManager.createSchemeObject(uniqueName, "")
        def createdSecurityLevel = createSecurityLevel("Security Level", "", createdScheme.id)

        def schemeEntity = new SchemeEntity(JiraPermissionHolderType.USER.getKey(), user.key, createdSecurityLevel.id)
        def schemeGV = issueSecuritySchemeManager.getScheme(createdScheme.id)
        issueSecuritySchemeManager.createSchemeEntity(schemeGV, schemeEntity)
    }

    def createSecurityLevel(String securityLevelName, String description, long schemeId) {
        def exists = { String name ->
            issueSecurityLevelManager.getIssueSecurityLevels(schemeId).any { it.name == name }
        }
        def uniqueName = ContextHelper.createUniqueName(securityLevelName, exists)
        issueSecurityLevelManager.createIssueSecurityLevel(schemeId, uniqueName, description)
    }

    def addIssueSecuritySchemeEntity(String schemeName, String securityLevelName, ApplicationUser user) {
        def scheme = issueSecuritySchemeManager.getSchemeObjects().find() { it.name == schemeName }
        def securityLevel = issueSecurityLevelManager.getIssueSecurityLevels(scheme.id).find { it.name == securityLevelName }

        def schemeEntity = new SchemeEntity(JiraPermissionHolderType.USER.getKey(), user.key, securityLevel.id)
        def schemeGV = issueSecuritySchemeManager.getScheme(scheme.id)
        issueSecuritySchemeManager.createSchemeEntity(schemeGV, schemeEntity)
    }

}
