package com.mgm_tp.atlas.jira.udc.helper

import com.atlassian.core.cron.parser.CronExpressionParser
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchRequest
import com.atlassian.jira.issue.subscription.SubscriptionManager
import com.atlassian.jira.sharing.SharedEntity.SharePermissions
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import org.apache.log4j.Logger

class FilterHelper {
    Logger log

    @JiraAgileBean
    UserManager userManager

    private SubscriptionManager subscriptionManager
    private SearchService searchService
    private SearchRequestService searchRequestService

    FilterHelper(Logger log) {
        this.log = log
        this.subscriptionManager = ComponentAccessor.subscriptionManager
        this.searchService = ComponentAccessor.getComponent(SearchService)
        this.searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
    }

    def createPrivateFilter(ApplicationUser user) {
        createFilter("Private Filter of " + user.username, user, SharePermissions.PRIVATE)
    }

    def createSharedFilter(ApplicationUser user) {
        createFilter("Shared Filter of " + user.username, user, ContextHelper.createUserEditPermissions(user, true))
    }

    def createPermittedFilter(List<ApplicationUser> users, ApplicationUser filterOwner) {
        def sharePermissions = ContextHelper.createEditPermissions(users)
        createFilter("Permitted Filter of " + filterOwner, filterOwner, sharePermissions)
    }

    def createPersonalSubscribedFilter(ApplicationUser subscribedUser, ApplicationUser filterOwner) {
        createSubscribedFilter(subscribedUser, filterOwner, null)
    }

    def createGroupSubscribedFilter(ApplicationUser user, ApplicationUser filterOwner) {
        def randomGroup = new UserHelper(log).getRandomGroups().get(0)
        createSubscribedFilter(user, filterOwner, randomGroup.name)
    }

    private def createSubscribedFilter(ApplicationUser user, ApplicationUser filterOwner, String group) {
        def sharePermissions = SharePermissions.AUTHENTICATED
        def createdFilter = createFilter("Subscribed Filter of " + filterOwner, filterOwner, sharePermissions)

        subscriptionManager.createSubscription(user, createdFilter.id, (String) group, CronExpressionParser.DEFAULT_CRONSTRING, false)
    }

    def createFilter(String filterName, ApplicationUser user, SharePermissions permissions) {
        def serviceContext = ContextHelper.getUserContext(user)
        def parseResult = searchService.parseQuery(user, "")
        def filterNameExists = { String name -> searchRequestService.getOwnedFilters(user).any { it.name == name } }
        def searchRequest = new SearchRequest(parseResult.query, user, ContextHelper.createUniqueName(filterName, filterNameExists), "")
        searchRequest.setPermissions(permissions)

        searchRequestService.validateFilterForCreate(serviceContext, searchRequest)
        searchRequestService.createFilter(serviceContext, searchRequest)
    }
}