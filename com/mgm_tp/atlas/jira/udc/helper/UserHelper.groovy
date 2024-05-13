package com.mgm_tp.atlas.jira.udc.helper

import com.adaptavist.hapi.jira.users.Users
import com.atlassian.jira.avatar.AvatarManager
import com.atlassian.jira.avatar.AvatarService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.icon.IconOwningObjectId
import com.atlassian.jira.icon.IconType
import com.atlassian.jira.security.auth.rememberme.JiraRememberMeTokenDao
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.user.ApplicationUser
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import com.atlassian.seraph.service.rememberme.DefaultRememberMeTokenGenerator
import org.apache.log4j.Logger

class UserHelper {

    Logger log

    @JiraAgileBean
    private AvatarService avatarService

    @JiraAgileBean
    private GroupManager groupManager

    private AvatarManager avatarManager
    private DefaultRememberMeTokenGenerator rememberMeTokenGenerator
    private JiraRememberMeTokenDao jiraRememberMeTokenDao

    UserHelper(Logger log) {
        this.log = log
        this.avatarManager = ComponentAccessor.avatarManager
        this.rememberMeTokenGenerator = ComponentAccessor.getComponent(DefaultRememberMeTokenGenerator)
        this.jiraRememberMeTokenDao = ComponentAccessor.getComponent(JiraRememberMeTokenDao)
    }

    def createUser() {
        def fullName = generateRandomName(4, 7)
        def username = 'tst-' + fullName.toLowerCase().split(' ').join()
        def email = username + '@example.com'

        createUser(username, email, fullName)
    }

    def createUser(String username, String email, String fullName) {
        def user = Users.create(username, email, fullName) {
            password = username
        }

        user
    }

    def updateDefaultAvatar(ApplicationUser user) {
        def avatarFileName = 'atlassian-logo.png'
        def pathToAssets = ContextHelper.getCurrentDir() + '/com/mgm_tp/atlas/jira/udc/assets'
        updateAvatar(user, pathToAssets, avatarFileName)
    }

    def addToRandomGroups(ApplicationUser user) {
        def randomGroups = getRandomGroups()

        randomGroups.forEach { group ->
            {
                groupManager.addUserToGroup(user, group)
            }
        }
    }

    private def updateAvatar(ApplicationUser user, String pathToDirectory, String fileName) {
        def avatarFilePath = pathToDirectory + "/" + fileName
        def file = new File(avatarFilePath)
        if (!file.exists()) {
            log.error('No file found at ' + avatarFilePath)
        }
        def contentType = AvatarManager.PNG_CONTENT_TYPE
        def iconType = IconType.USER_ICON_TYPE
        def owner = new IconOwningObjectId(user.key)
        def iStream = new BufferedInputStream(new FileInputStream(file))
        def newAvatar = avatarManager.create(fileName, contentType, iconType, owner, iStream, null)

        avatarService.setCustomUserAvatar(user, user, newAvatar.getId())
    }

    def generateRememberMeToken(String username) {
        def newToken = rememberMeTokenGenerator.generateToken(username)
        jiraRememberMeTokenDao.save(newToken)
    }

    def generatePersonalAccessToken(String tokenName, int expireDuration, String username, String password) {
        def restHelper = new RestHelper(log)
        def body = [
                name              : tokenName,
                expirationDuration: expireDuration
        ]
        def response = restHelper.sendPostRequest(RestHelper.PAT_CREATION_PATH, username, password, body)

        if (response && response.status == 201) {
            def responseObject = response.getData()
            log.warn responseObject

            return responseObject.rawToken
        }

        response
    }

    def getRememberMeTokens(String username) {
        jiraRememberMeTokenDao.findForUserName(username)
    }

    static def getCurrentAdmin() {
        ComponentAccessor.jiraAuthenticationContext.loggedInUser
    }

    def getRandomGroups() {
        def groups = ComponentAccessor.groupManager.getAllGroups()
        groups.removeIf { it.name.toLowerCase().contains('administrator') }
        def n = (int) (Math.random() * Math.min(groups.size(), 10)) + 1
        def randomGroups = groups.toList().shuffled()[0..(n - 1)]

        if (randomGroups.empty) {
            return [groupManager.createGroup('Group for testing')]
        }

        randomGroups
    }

    static def generateRandomName(int minLength, int maxLength) {
        def nameLength1 = new Random().nextInt(maxLength - minLength + 1) + minLength
        def nameLength2 = new Random().nextInt(maxLength - minLength + 1) + minLength
        def firstName = generateRandomString(nameLength1)
        def lastName = generateRandomString(nameLength2)

        firstName.capitalize() + " " + lastName.capitalize()
    }

    private static def generateRandomString(int length) {
        def chars = ('a'..'z')
        def random = new Random()

        (1..length).collect { chars[random.nextInt(chars.size())] }.join()
    }

}
