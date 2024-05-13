package com.mgm_tp.atlas.jira.udc.helper

import com.atlassian.jira.user.ApplicationUser
import org.apache.log4j.Logger

class TestDataGenerator {

    Logger log

    private ProjectHelper projectHelper
    private DashboardHelper dashboardHelper
    private FilterHelper filterHelper
    private AgileBoardHelper agileBoardHelper
    private SchemeHelper schemeHelper
    private UserHelper userHelper
    private ContextHelper contextHelper
    public services

    def TestDataGenerator(Logger log) {
        this.log = log
        projectHelper = new ProjectHelper(log)
        dashboardHelper = new DashboardHelper(log)
        filterHelper = new FilterHelper(log)
        agileBoardHelper = new AgileBoardHelper(log)
        schemeHelper = new SchemeHelper(log)
        userHelper = new UserHelper(log)
        contextHelper = new ContextHelper(log)

        services = new HashMap()

        services.put(GenerateDataOptions.PROJECT_LEAD_PERMISSION.toString(), (ApplicationUser newUser, String projectKey) -> projectHelper.changeProjectLead(newUser.username, projectKey))
        services.put(GenerateDataOptions.PROJECT_PERMISSION_ROLE.toString(), (ApplicationUser newUser, String projectKey) -> projectHelper.addProjectRole([newUser], projectKey))
        services.put(GenerateDataOptions.COMPONENT_LEAD_PERMISSION.toString(), (ApplicationUser newUser, String projectKey) -> projectHelper.createComponentLead(newUser, projectKey))
        services.put(GenerateDataOptions.PRIVATE_DASHBOARD.toString(), (ApplicationUser newUser) -> dashboardHelper.createPrivateDashboard(newUser))
        services.put(GenerateDataOptions.SHARED_DASHBOARD.toString(), (ApplicationUser newUser) -> dashboardHelper.createSharedDashboard(newUser))
        services.put(GenerateDataOptions.PERMITTED_DASHBOARD.toString(), (ApplicationUser newUser) -> dashboardHelper.createPermittedDashboard([newUser], userHelper.getCurrentAdmin()))
        services.put(GenerateDataOptions.PRIVATE_FILTER.toString(), (ApplicationUser newUser) -> filterHelper.createPrivateFilter(newUser))
        services.put(GenerateDataOptions.SHARED_FILTER.toString(), (ApplicationUser newUser) -> filterHelper.createSharedFilter(newUser))
        services.put(GenerateDataOptions.PERMITTED_FILTER.toString(), (ApplicationUser newUser) -> filterHelper.createPermittedFilter([newUser], userHelper.getCurrentAdmin()))
        services.put(GenerateDataOptions.PERSONAL_SUBSCRIBED_FILTER.toString(), (ApplicationUser newUser) -> filterHelper.createPersonalSubscribedFilter(newUser, userHelper.getCurrentAdmin()))
        services.put(GenerateDataOptions.GROUP_SUBSCRIBED_FILTER.toString(), (ApplicationUser newUser) -> filterHelper.createGroupSubscribedFilter(newUser, userHelper.getCurrentAdmin()))
        services.put(GenerateDataOptions.PRIVATE_AGILEBOARD.toString(), (ApplicationUser newUser) -> agileBoardHelper.createPrivateAgileBoard(newUser))
        services.put(GenerateDataOptions.SHARED_AGILEBOARD.toString(), (ApplicationUser newUser) -> agileBoardHelper.createSharedAgileBoard(newUser))
        services.put(GenerateDataOptions.PERMITTED_AGILEBOARD.toString(), (ApplicationUser newUser) -> agileBoardHelper.createPermittedAgileBoard([newUser], userHelper.getCurrentAdmin()))
        services.put(GenerateDataOptions.PERMISSION_SCHEME.toString(), (ApplicationUser newUser) -> schemeHelper.createPermissionScheme(newUser))
        services.put(GenerateDataOptions.NOTIFICATION_SCHEME.toString(), (ApplicationUser newUser) -> schemeHelper.createNotificationScheme(newUser))
        services.put(GenerateDataOptions.ISSUE_SECURITY_SCHEME.toString(), (ApplicationUser newUser) -> schemeHelper.createIssueSecurityScheme(newUser))
        services.put(GenerateDataOptions.CF_USER_DEFAULT_CONTEXT.toString(), (ApplicationUser newUser, String customFieldId) -> contextHelper.createContextWithDefaultValue(customFieldId, newUser))
        services.put(GenerateDataOptions.AVATAR.toString(), (ApplicationUser newUser) -> userHelper.updateDefaultAvatar(newUser))
        services.put(GenerateDataOptions.PERSONAL_ACCESS_TOKEN.toString(), (ApplicationUser newUser) -> userHelper.generatePersonalAccessToken(newUser.username + ' PAT', 90, newUser.username, newUser.username))
        services.put(GenerateDataOptions.REMEMBER_ME_TOKEN.toString(), (ApplicationUser newUser) -> userHelper.generateRememberMeToken(newUser.username) )
        services.put(GenerateDataOptions.GROUPS.toString(), (ApplicationUser newUser) -> userHelper.addToRandomGroups(newUser) )
    }

    public createTestUser(String[] options, String projectKey, String customFieldId) {
        def newUser = userHelper.createUser()
        updateUser(newUser, options, projectKey, customFieldId)
    }

    public updateUser(ApplicationUser user, String[] options, String projectKey, String customFieldId) {
        options.each { option ->
            {
                def sv = services.get(option)
                if (sv) {
                    if (option == GenerateDataOptions.PROJECT_LEAD_PERMISSION.toString()
                            || option == GenerateDataOptions.PROJECT_PERMISSION_ROLE.toString()
                            || option == GenerateDataOptions.COMPONENT_LEAD_PERMISSION.toString()) {
                        sv(user, projectKey)
                    } else if (option == GenerateDataOptions.CF_USER_DEFAULT_CONTEXT.toString()) {
                        sv(user, customFieldId)
                    } else {
                        sv(user)
                    }
                }
            }
        }
        log.warn 'Data generated for user ' + user
        user
    }
}
