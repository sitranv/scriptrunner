package com.mgm_tp.atlas.jira.udc.helper

import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.config.FieldConfigScheme
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.sharing.SharePermission
import com.atlassian.jira.sharing.SharePermissionImpl
import com.atlassian.jira.sharing.SharedEntity
import com.atlassian.jira.sharing.rights.ShareRights
import com.atlassian.jira.sharing.type.ShareType
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.onresolve.scriptrunner.runner.ScriptRunner
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import org.apache.log4j.Logger

class ContextHelper {

    Logger log

    @JiraAgileBean
    private UserManager userManager

    @JiraAgileBean
    private FieldConfigSchemeManager fieldConfigSchemeManager

    @JiraAgileBean
    private IssueTypeSchemeManager issueTypeSchemeManager

    @JiraAgileBean
    private CustomFieldManager customFieldManager

    private FieldConfigManager fieldConfigManager

    ContextHelper(Logger log) {
        this.log = log
        this.fieldConfigManager = ComponentAccessor.getComponent(FieldConfigManager)
    }

    def createContextWithDefaultValue(String cfFieldId, ApplicationUser user) {
        def cf = customFieldManager.getCustomFieldObject(cfFieldId)
        if (cf == null) {
            log.error "No custom field " + cfFieldId + " exists"
            return
        }
        def newContext = createContextForCustomField(cf, "Context of " + cf.name + " for " + user.name)
        updateDefaultValue(newContext, user, cf)
    }

    def createContextForCustomField(CustomField cf, String customFieldName) {
        def issueTypes = issueTypeSchemeManager.getIssueTypesForDefaultScheme()
        FieldConfigScheme.Builder builder = new FieldConfigScheme.Builder()
        builder.setName(customFieldName)
        builder.setDescription('')
        builder.setFieldId(cf.id)
        def newFieldConfigScheme = builder.toFieldConfigScheme()

        fieldConfigSchemeManager.createFieldConfigScheme(newFieldConfigScheme, newFieldConfigScheme.contexts, issueTypes as List<IssueType>, newFieldConfigScheme.field)
    }

    static def updateDefaultValue(FieldConfigScheme fieldConfigScheme, ApplicationUser user, CustomField cf) {
        fieldConfigScheme.configs.values().forEach { cf.getCustomFieldType().setDefaultValue(it, user) }
    }

    List getUserContext(String username) {
        def user = userManager.getUserByName(username)
        def serviceContext = getUserContext(user)

        [user, serviceContext]
    }

    static def getUserContext(ApplicationUser user) { new JiraServiceContextImpl(user) }

    static def createUniqueName(String name, Closure<Boolean> exists) {
        int counter = 0
        def uniqueName = name
        while (exists(uniqueName)) {
            counter++
            uniqueName = name + " (" + counter + ")"
        }

        uniqueName
    }

    static def getCurrentDir() {
        def scriptRoots = ScriptRunnerImpl.getPluginComponent(ScriptRunner).getRootsForDisplay()?.split(", ")?.toList()
        scriptRoots.get(0)
    }

    static def getBaseUrl() {
        ComponentAccessor.applicationProperties.getJiraBaseUrl()
    }

    static def createEditPermissions(List<ApplicationUser> users) {
        def sharePermissions = []
        for (ApplicationUser user : users) {
            if (!user) continue
            def sharePermission = new SharePermissionImpl(ShareType.Name.USER, user.key, (String) null, ShareRights.VIEW_EDIT)
            sharePermissions.add(sharePermission)

        }
        new SharedEntity.SharePermissions(sharePermissions as Set)
    }

    static def createUserEditPermissions(ApplicationUser user, boolean includeViewEdit) {
        def ownerEditPermission = new SharePermissionImpl(ShareType.Name.USER, user.getKey(), (String)null, ShareRights.VIEW_EDIT)
        def permissions = new HashSet<SharePermission>()
        permissions.add(ownerEditPermission)
        if (includeViewEdit)
            permissions.add(SharedEntity.SharePermissions.AUTHENTICATED[0])

        new SharedEntity.SharePermissions(permissions)
    }
}
