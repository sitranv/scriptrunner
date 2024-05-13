package com.mgm_tp.atlas.jira.udc.helper

import com.atlassian.greenhopper.manager.rapidview.BoardAdminManager
import com.atlassian.greenhopper.model.rapid.BoardAdmin
import com.atlassian.greenhopper.service.rapid.view.RapidViewService
import com.atlassian.greenhopper.web.rapid.view.RapidViewPreset
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import org.apache.log4j.Logger

@WithPlugin('com.pyxis.greenhopper.jira')

class AgileBoardHelper {
    Logger log

    @JiraAgileBean
    private RapidViewService rapidViewService

    @JiraAgileBean
    private BoardAdminManager boardAdminManager

    @JiraAgileBean
    private UserManager userManager

    AgileBoardHelper(Logger log) {
        this.log = log
    }

    def createPrivateAgileBoard(ApplicationUser owner) {
        createAgileBoard(owner, true)
    }

    def createSharedAgileBoard(ApplicationUser owner) {
        createAgileBoard(owner, false)
    }

    def createPermittedAgileBoard(List<ApplicationUser> users, ApplicationUser agileBoardOwner) {
        def result = createPrivateAgileBoard(agileBoardOwner)
        log.warn(boardAdminManager)
        if (result.valid) {
            def agileBoard = result.value
            def boardAdmins = copyBoardAdmins(boardAdminManager.getBoardAdmins(agileBoard))
            for (ApplicationUser user : users) {
                if (!user) continue
                def newAdmin = createBoardAdmin(user.key, BoardAdmin.Type.USER)
                boardAdmins.add(newAdmin)
            }

            boardAdminManager.updateBoardAdmin(agileBoard, boardAdmins)
        }
    }

    def createAgileBoard(ApplicationUser owner, boolean isPrivate) {
        def savedFilter = createFilter(owner, isPrivate)
        def name = isPrivate ? "Private Agile Board" : "Shared Agile Board"
        name += " of " + owner.username

        rapidViewService.create(owner, name, savedFilter.id, RapidViewPreset.SCRUM)
    }

    private createFilter(ApplicationUser owner, boolean isPrivate) {
        def fu = new FilterHelper(log)
        isPrivate ? fu.createPrivateFilter(owner) : fu.createSharedFilter(owner)
    }

    private static def createBoardAdmin(String key, BoardAdmin.Type type) {
        new BoardAdmin.RapidViewBoardAdminBuilder()
                .key(key)
                .type(type)
                .build()
    }

    private static def copyBoardAdmins(List<BoardAdmin> boardAdmins) {
        def newAdmins = new ArrayList()
        for (BoardAdmin boardAdmin : boardAdmins) {
            newAdmins.add(createBoardAdmin(boardAdmin.key, boardAdmin.type))
        }

        newAdmins
    }
}