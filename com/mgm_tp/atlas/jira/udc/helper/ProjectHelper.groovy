package com.mgm_tp.atlas.jira.udc.helper

import com.adaptavist.hapi.jira.projects.Projects
import com.atlassian.jira.bc.project.component.ProjectComponentManager
import com.atlassian.jira.bc.projectroles.ProjectRoleService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.project.AssigneeTypes
import com.atlassian.jira.security.roles.ProjectRoleActor
import com.atlassian.jira.security.roles.ProjectRoleImpl
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import org.apache.log4j.Logger

class ProjectHelper {
    Logger log

    @JiraAgileBean
    private UserManager userManager

    @JiraAgileBean
    private ProjectComponentManager projectComponentManager

    private ProjectRoleManager projectRoleManager
    private ProjectRoleService projectRoleService

    ProjectHelper(Logger log) {
        this.log = log
        this.projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
        this.projectRoleService = ComponentAccessor.getComponent(ProjectRoleService)
    }

    def addProjectRole(List<ApplicationUser> users, String projectKey) {
        def randomRole = projectRoleManager.getProjectRoles()[0]
        if (!randomRole) {
            log.warn('There is no Default Project Role')
            randomRole = createNewProjectRole()
        }
        def project = Projects.getByKey(projectKey)
        if (project == null) {
            log.error "No project " + projectKey + " exists"
            return
        }
        projectRoleService.addActorsToProjectRole(users.key, randomRole, project, ProjectRoleActor.USER_ROLE_ACTOR_TYPE, new SimpleErrorCollection())
    }

    def createNewProjectRole() {
        def newPrjRole = new ProjectRoleImpl('Project Role for testing','')
        projectRoleManager.createRole(newPrjRole)
    }

    def changeProjectLead(String username, String projectKey) {
        def project = Projects.getByKey(projectKey)
        if (project == null) {
            log.error "No project " + projectKey + " exists"
            return
        }

        project.update {
            projectLead = username
        }
    }

    def createComponentLead(ApplicationUser user, String projectKey) {
        createComponent(user, projectKey, "Component Lead", AssigneeTypes.COMPONENT_LEAD)
    }

    def updateProjectLead(String username, String projectKey){
        Projects.getByKey(projectKey).update {
            projectLead = username
        }
    }

    private def createComponent(ApplicationUser user, String projectKey, String componentName, long componentId) {
        def project = Projects.getByKey(projectKey)
        if (project == null) {
            log.error "No project " + projectKey + " exists"
            return
        }
        def exists = {String name -> projectComponentManager.findByComponentName(project.id, name) != null }
        def uniqueName = ContextHelper.createUniqueName(componentName, exists)

        projectComponentManager.create(uniqueName, '', user.key, componentId, project.id)
    }
}
