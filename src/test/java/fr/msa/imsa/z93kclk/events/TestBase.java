package fr.msa.imsa.z93kclk.events;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.keycloak.OAuth2Constants;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.utils.MediaType;

import java.util.Map;

import static io.restassured.RestAssured.given;

@SuppressWarnings({"unused", "SameParameterValue"})
public class TestBase {

    protected ValidatableResponse requestToken(KeycloakContainer keycloak, String realm, String username, String password) {
        return requestToken(keycloak, realm, username, password, 200);
    }

    protected ValidatableResponse requestToken(KeycloakContainer keycloak, String realm, String username, String password, int expectedStatusCode) {
        String tokenEndpoint = getOpenIDConfiguration(keycloak, realm)
                .extract().path("token_endpoint");
        return RestAssured.given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam(OAuth2Constants.USERNAME, username)
                .formParam(OAuth2Constants.PASSWORD, password)
                .formParam(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD)
                .formParam(OAuth2Constants.CLIENT_ID, KeycloakContainer.ADMIN_CLI_CLIENT)
                .formParam(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID)
                .when().post(tokenEndpoint)
                .then().statusCode(expectedStatusCode);
    }

    protected ValidatableResponse getOpenIDConfiguration(KeycloakContainer keycloak, String realm) {
        return RestAssured.given().pathParam("realm-name", realm)
                .when().get(keycloak.getAuthServerUrl() + ServiceUrlConstants.DISCOVERY_URL)
                .then().statusCode(200);
    }

    protected ValidatableResponse oidcClientLogin(KeycloakContainer keycloak, String realm, String clientId, String username, String password) {
        // calls authorization_endpoint
        String authEndpoint = getOpenIDConfiguration(keycloak, realm).extract().path("authorization_endpoint");
        ExtractableResponse<Response> responseFromAuthEndpoint = given()
                .queryParam(OAuth2Constants.RESPONSE_TYPE, OAuth2Constants.CODE)
                .queryParam(OAuth2Constants.CLIENT_ID, clientId)
                .queryParam(OAuth2Constants.REDIRECT_URI, keycloak.getAuthServerUrl() + "/realms/" + realm + "/")
                .queryParam(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID)
                .when().get(authEndpoint)
                .then().statusCode(200).extract();
        Map<String, String> cookies = responseFromAuthEndpoint.cookies();
        String loginFormActionUrl = responseFromAuthEndpoint.htmlPath().getString("html.body.div.div.div.div.div.div.form.@action");

        // authenticate with login/password
        return given().cookies(cookies)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", username)
                .formParam("password", password)
                .when().post(loginFormActionUrl)
                .then().statusCode(200);

    }
}
