package fr.msa.imsa.z93kclk.events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
public class LastLoginTimeListenerTest extends TestBase {

	private static final String REALM = "demo";

	@Container
	private static final KeycloakContainer keycloak = new KeycloakContainer()
		.withRealmImportFile("demo-realm.json")
		.withEnv("KC_SPI_EVENTS_LISTENER_LAST_LOGIN_TIME_ATTRIBUTE_NAME", "lastLogin")
		.withProviderClassesFrom("target/classes");

	@Test
	public void testLastLoginTime() throws InterruptedException {
		keycloak.start();
		Keycloak admin = keycloak.getKeycloakAdminClient();
		// check user has no attributes
		List<UserRepresentation> users = admin.realm(REALM).users().searchByUsername("test", true);
		UserRepresentation testUser = users.get(0);
		Map<String, List<String>> attributes = testUser.getAttributes();
		assertNull(attributes);

		// configure custom events listener
		RealmEventsConfigRepresentation eventsConfig = new RealmEventsConfigRepresentation();
		eventsConfig.setEventsListeners(List.of(LastLoginTimeListenerFactory.PROVIDER_ID));
		admin.realm(REALM).updateRealmEventsConfig(eventsConfig);

		// "login" user
		requestToken(keycloak, REALM, "test", "test");

		// check last log
		String[] fullLog = keycloak.getLogs().split("\n");
		String lastLog = fullLog[fullLog.length - 1];
		assertThat(lastLog, containsString("Ceci est un log info sur un event de LOGIN"));
		// check user has last-login-time attribute
		testUser = admin.realm(REALM).users().searchByUsername("test", true).get(0);
		String lastLoginTime = testUser.firstAttribute("lastLogin");
		assertNotNull(lastLoginTime);
	}

}
