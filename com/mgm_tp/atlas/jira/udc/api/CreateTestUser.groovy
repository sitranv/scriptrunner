package com.mgm_tp.atlas.jira.udc.api

import com.mgm_tp.atlas.jira.udc.helper.*
import groovy.json.JsonSlurper
import groovy.transform.BaseScript
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

enum Options {
    PROJECT_LEAD_PERMISSION("PROJECT_LEAD_PERMISSION"),
    COMPONENT_LEAD_PERMISSION("COMPONENT_LEAD_PERMISSION"),
    PRIVATE_DASHBOARD("PRIVATE_DASHBOARD"),
    SHARED_DASHBOARD("SHARED_DASHBOARD"),
    PRIVATE_FILTER("PRIVATE_FILTER"),
    SHARED_FILTER("SHARED_FILTER"),
    PRIVATE_AGILEBOARD("PRIVATE_AGILEBOARD"),
    SHARED_AGILEBOARD("SHARED_AGILEBOARD"),
    PERMITTED_FILTER("PERMITTED_FILTER"),
    PERMITTED_DASHBOARD("PERMITTED_DASHBOARD"),
    PERMITTED_AGILEBOARD("PERMITTED_AGILEBOARD"),
    PERSONAL_SUBSCRIBED_FILTER("PERSONAL_SUBSCRIBED_FILTER"),
    GROUP_SUBSCRIBED_FILTER("GROUP_SUBSCRIBED_FILTER"),
    PROJECT_PERMISSION_ROLE("PROJECT_PERMISSION_ROLE"),
    CF_USER_DEFAULT_CONTEXT("CF_USER_DEFAULT_CONTEXT"),
    PERMISSION_SCHEME("PERMISSION_SCHEME"),
    NOTIFICATION_SCHEME("NOTIFICATION_SCHEME"),
    ISSUE_SECURITY_SCHEME("ISSUE_SECURITY_SCHEME"),
    PERSONAL_ACCESS_TOKEN("PERSONAL_ACCESS_TOKEN"),
    REMEMBER_ME_TOKEN("REMEMBER_ME_TOKEN"),
    REMOVE_AVATAR("REMOVE_AVATAR"),
    CHANGE_EMAIL("CHANGE_EMAIL"),
    CHANGE_NAME("CHANGE_NAME"),
    USER_GROUP("USER_GROUP")

    final String option

    // Constructor to assign the display name
    Options(String option) {
        this.option = option
    }

    // Override toString() to return the display name
    String toString() {
        option
    }
}

def projectHelper = new ProjectHelper(log)
def dashboardHelper = new DashboardHelper(log)
def filterHelper = new FilterHelper(log)
def agileBoardHelper = new AgileBoardHelper(log)
def schemeHelper = new SchemeHelper(log)
def userHelper = new UserHelper(log)
def contextHelper = new ContextHelper(log)
final allowedGroups = ['jira-administrators']

@BaseScript CustomEndpointDelegate delegate
createTestUser(httpMethod: "POST", groups: allowedGroups) { MultivaluedMap queryParams, String body ->
    def form = new JsonSlurper().parseText(body) as Map<String, List>
    def username = form.username as String
    def email = form.email as String
    def fullName = form.fullName as String
    def newUser

    if (username && email && fullName) {
        newUser = userHelper.createUser(username, email, fullName)
    } else {
        newUser = userHelper.createUser()
    }

    def options = form.options as String[]
    def projectKey = form.projectKey as String
    def customFieldId = form.customFieldId as String

    Response.ok().entity(new TestDataGenerator(log).updateUser(newUser, options, projectKey, customFieldId).toString()).build()
}

