# MGM SCRIPT: Create test user

+ Create a test user with sample data based on your options
+ Basic authentication with username and password from an administrator in "jira-administrators"
  group
---
## Configuration

### Precondition
- ScriptRunner for Jira installed
- Username and password from an Jira administrator for basic authentication

### Installation
- Obtain the script from this repository [source].
- Copy the entire contents of this repository to the 'scripts' folder located at <your_jira_home>/scripts.
---
## Usage
- ScriptRunner console
- ScriptRunner REST Endpoints

### ScriptRunner console
To create a test user with a random name, email address, and full name, run the following script from the script console
```groovy
package com.mgm_tp.atlas.jira.udc.helper

def newUser = new TestDataGenerator(log).createTestUser(null, null, null)
```

If you want to create a user with some extra data, add options to it:
```groovy
package com.mgm_tp.atlas.jira.udc.helper

String[] options = [
        "PROJECT_LEAD_PERMISSION",
        "PROJECT_PERMISSION_ROLE",
        "COMPONENT_LEAD_PERMISSION",
        "PRIVATE_DASHBOARD",
        "SHARED_DASHBOARD",
        "PERMITTED_DASHBOARD",
        "PRIVATE_FILTER",
        "PERMITTED_FILTER",
        "SHARED_FILTER",
        "PERSONAL_SUBSCRIBED_FILTER",
        "GROUP_SUBSCRIBED_FILTER",
        "PRIVATE_AGILEBOARD",
        "SHARED_AGILEBOARD",
        "PERMITTED_AGILEBOARD",
        "PERMISSION_SCHEME",
        "NOTIFICATION_SCHEME",
        "ISSUE_SECURITY_SCHEME",
        "CF_USER_DEFAULT_CONTEXT",
        "REMEMBER_ME_TOKEN",
        "GROUPS",
        "PERSONAL_ACCESS_TOKEN",
        "AVATAR"
]
def prjKey = 'TEST'
def cfId = 'customfield_10301'
def newUser = new TestDataGenerator(log).createTestUser(options, prjKey, cfId)
```
You can also generate test data for a specific user
```groovy
package com.mgm_tp.atlas.jira.udc.helper
import com.atlassian.jira.component.ComponentAccessor

String[] options = [
    "PRIVATE_DASHBOARD",
    "SHARED_DASHBOARD",
    "PRIVATE_FILTER",
    "SHARED_FILTER"
]
def user = ComponentAccessor.userManager.getUserByName('aaomzdtbx')
def updatedUser = new TestDataGenerator(log).updateUser(user, options, null, null)
```

---
### ScriptRunner REST Endpoints
#### Configuration
- In script runner plugin -> tab REST Endpoints
- Click "Create REST Endpoint"
- Enter an optional "Note" for your reference
- Copy the path of "CreateTestUser.groovy" to "File" section
- Click "Add"

#### How to use the API
Request
```
curl --request POST \
--url <jira_base_url>/rest/scriptrunner/latest/custom/createTestUser \
--data <your_request_body>
```

Example json body
```json
{
  "projectKey": "TEST",
  "customFieldId": "customfield_10301",
  "options": [
    "PROJECT_LEAD_PERMISSION",
    "PROJECT_PERMISSION_ROLE",
    "COMPONENT_LEAD_PERMISSION",
    "PRIVATE_DASHBOARD",
    "SHARED_DASHBOARD",
    "PERMITTED_DASHBOARD",
    "PRIVATE_FILTER",
    "PERMITTED_FILTER",
    "SHARED_FILTER",
    "PERSONAL_SUBSCRIBED_FILTER",
    "GROUP_SUBSCRIBED_FILTER",
    "PRIVATE_AGILEBOARD",
    "SHARED_AGILEBOARD",
    "PERMITTED_AGILEBOARD",
    "PERMISSION_SCHEME",
    "NOTIFICATION_SCHEME",
    "ISSUE_SECURITY_SCHEME",
    "CF_USER_DEFAULT_CONTEXT",
    "REMEMBER_ME_TOKEN",
    "GROUPS",
    "PERSONAL_ACCESS_TOKEN",
    "AVATAR"
  ]
}
```

For a specific username, email and fullName, you could add these additional data to your body:

```json
{
  "username": "tst-jira-administrators",
  "email": "tst-jira-administrators@gmail.com",
  "fullName": "Test Jira Administrator"
}
```
---
## Options description
- ```PROJECT_LEAD_PERMISSION``` Change your test project lead to the created user
- ```PROJECT_PERMISSION_ROLE```Add a project role to your test project with value as the created user
- ```COMPONENT_LEAD_PERMISSION```Create a component lead in your test project with value as the created user
- ```PRIVATE_DASHBOARD``` Create a private dashboard of the created user
- ```SHARED_DASHBOARD``` Create a shared dashboard of the created user
- ```PERMITTED_DASHBOARD``` Create a shared dashboard of the administrator, the created user will have the edit permission on this dashboard
- ```PRIVATE_FILTER``` Create a private filter of the created user
- ```SHARED_FILTER``` Create a shared filter of the created user
- ```PERMITTED_FILTER``` Create a shared filter of the administrator, the created user will have the edit permission on this dashboard
- ```PERSONAL_SUBSCRIBED_FILTER``` Create a shared filter of the administrator, the created user will have a personal filter subscription on this filter
- ```GROUP_SUBSCRIBED_FILTER``` Create a shared filter of the administrator, the created user will have a group filter subscription on this filter
- ```PRIVATE_AGILEBOARD``` Create a private agile board of the created user
- ```SHARED_AGILEBOARD``` Create a shared agile board of the created user
- ```PERMITTED_AGILEBOARD``` Create a shared agile board of the administrator, the created user is also the agile board administrator
- ```PERMISSION_SCHEME``` Create a permission scheme with value of the created user
- ```NOTIFICATION_SCHEME``` Create a notification scheme with value of the created user
- ```ISSUE_SECURITY_SCHEME``` Create a issue security scheme with value of the created user
- ```CF_USER_DEFAULT_CONTEXT``` Create a context of your custom field with value of the created user
- ```AVATAR``` Create a default avatar
- ```GROUPS``` Add this user to some random groups except for 'administrators'
- ```PERSONAL_ACCESS_TOKEN``` Create a personal access token
- ```REMEMBER_ME_TOKEN``` Create a remember me token

## Troubleshooting

If you encounter any issues while using this script, try the following troubleshooting steps:

1. **Script Execution Time Issue**: Try not to use ```AVATAR``` and ```PERSONAL_ACCESS_TOKEN``` options. These options could take much time in some cases 

2. **NullPointerException**: If you encounter an error message, carefully review it for clues on what might be causing the issue. Sometimes, error messages provide valuable information that can help in troubleshooting.

