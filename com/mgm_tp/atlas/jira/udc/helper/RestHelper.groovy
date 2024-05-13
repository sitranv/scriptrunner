package com.mgm_tp.atlas.jira.udc.helper

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.apache.log4j.Logger

class RestHelper {

    static final PAT_CREATION_PATH = ContextHelper.getBaseUrl() + '/rest/pat/latest/tokens'

    Logger log

    RestHelper(Logger log) {
        this.log = log
    }

    def sendPostRequest(String url, String username, String password, Map<String, Serializable> body) {
        def authHeader = createBasicAuthHeader(username, password)
        def jsonBody = JsonOutput.toJson(body)
        def restClient = new RESTClient(url)

        def response

        try {
            response = restClient.post(
                    body: jsonBody,
                    requestContentType: 'application/json',
                    headers: [
                            Authorization: authHeader
                    ]
            )
        } catch (Exception e) {
            log.warn "Error occurred: ${e.message}"
            return
        }

        response
    }

    static private def createBasicAuthHeader(String username, String password) {
        def authHeader = "Basic " + "${username}:${password}".bytes.encodeBase64().toString()

        authHeader
    }
}
