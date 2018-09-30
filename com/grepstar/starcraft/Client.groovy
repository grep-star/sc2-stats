package com.grepstar.starcraft

import io.restassured.RestAssured
import io.restassured.http.Headers
import io.restassured.response.Response

@SuppressWarnings('GrMethodMayBeStatic')
class Client {

    String apiKey
    String oauthToken

    Client(String apiKey, String oauthToken) {
        setApiKey(apiKey)
        setOauthToken(oauthToken)
    }

    Response requestUsingApiKey(String url) {
        request('apikey', apiKey, url)
    }

    Response requestUsingOauthToken(String url) {
        request('access_token', oauthToken, url)
    }

    private Response request(String authParam, String authValue, String url) {
        final Response response = RestAssured.given().queryParam(authParam, authValue).get(url)
        final Headers headers = response.headers()
        if (allotmentExhausted(headers, 'X-Plan-QPS-Allotted', 'X-Plan-QPS-Current')) {
            println('Maximum number of queries per second has been reached. Pausing for a second...')
            sleep(1000)
        }
        if (allotmentExhausted(headers, 'X-Plan-Quota-Allotted', 'X-Plan-Quota-Current')) {
            println('Maximum number of queries per hour has been reached. Pausing for an hour...')
            sleep(3600000)
        }
        response
    }

    private boolean allotmentExhausted(Headers headers, String allottedHeader, String currentHeader) {
        (headers.hasHeaderWithName(allottedHeader) && headers.hasHeaderWithName(currentHeader)) &&
            (Integer.parseInt(headers.getValue(allottedHeader)) - Integer.parseInt(headers.getValue(currentHeader))) < 2
    }

}
