package com.mgm_tp.atlas.jira.udc.helper

import com.atlassian.gadgets.dashboard.Layout
import com.atlassian.jira.bc.JiraServiceContext
import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.portal.PortalPageService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.portal.PortalPage
import com.atlassian.jira.portal.PortalPageManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import org.apache.log4j.Logger

import static com.atlassian.jira.sharing.SharedEntity.SharePermissions

class DashboardHelper {
    Logger log

    @JiraAgileBean
    private UserManager userManager

    private PortalPageManager portalPageManager
    private PortalPageService portalPageService

    DashboardHelper(Logger log) {
        this.log = log
        this.portalPageManager = ComponentAccessor.getComponent(PortalPageManager)
        this.portalPageService = ComponentAccessor.getComponent(PortalPageService)
    }

    def createPrivateDashboard(ApplicationUser owner) {
        createDashboard('Private Dashboard of ' + owner.username, owner, SharePermissions.PRIVATE)
    }

    def createSharedDashboard(ApplicationUser owner) {
        createDashboard('Shared Dashboard of ' + owner.username, owner, ContextHelper.createUserEditPermissions(owner, true))
    }

    def createPermittedDashboard(List<ApplicationUser> permittedUsers, ApplicationUser dashboardOwner) {
        def sharePermissions = ContextHelper.createEditPermissions(permittedUsers)
        createDashboard("Permitted Dashboard of " + dashboardOwner, dashboardOwner, sharePermissions)
    }

    def createDashboard(String dashboardName, ApplicationUser owner, SharePermissions permissions) {
        def dashboardNameExists = { String name -> portalPageService.getOwnedPortalPages(owner).any { it.name == name } }
        def name = ContextHelper.createUniqueName(dashboardName, dashboardNameExists)

        def dashboardTemplate = new PortalPage.Builder()
                .name(name)
                .owner(owner)
                .layout(Layout.AA)
                .permissions(permissions)
                .build()
        JiraServiceContext sourceUserServiceCtx = new JiraServiceContextImpl(owner)

        if (!portalPageService.validateForCreate(sourceUserServiceCtx, dashboardTemplate)) {
            log.warn(sourceUserServiceCtx.errorCollection)
            return sourceUserServiceCtx.errorCollection
        }

        portalPageService.createPortalPage(sourceUserServiceCtx, dashboardTemplate)
    }

}