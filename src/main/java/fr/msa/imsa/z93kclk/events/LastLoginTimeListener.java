package fr.msa.imsa.z93kclk.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@Slf4j(topic = "org.keycloak.events")
@RequiredArgsConstructor
public class LastLoginTimeListener implements EventListenerProvider {

	private final KeycloakSession session;

	@Override
	public void onEvent(Event event) {
		if (event.getType().equals(EventType.LOGIN)) {
			UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
			if (user != null) {
				user.setSingleAttribute(LastLoginTimeListenerFactory.attributeName, Integer.toString(Time.currentTime()));
				log.info("Ceci est un log info sur un event de LOGIN");
			}
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
	}

	@Override
	public void close() {
	}

}
