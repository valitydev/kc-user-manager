package com.rbkmoney.kc.user.manager.keycloak;

import com.rbkmoney.kc.user.manager.config.properties.KeycloakAdminClientsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class KeycloakAdminClientManager {

    private final ConcurrentHashMap<String, Keycloak> registry = new ConcurrentHashMap<>();

    private final KeycloakAdminClientsProperties props;

    public Keycloak getKcClient(String realm) {
        log.info("Requesting keycloak admin client for realm {}", realm);
        if (!props.getAdminClients().containsKey(realm)) {
            throw new IllegalArgumentException("Realm " + realm + " is not defined");
        }

        Keycloak keycloak = registry.get(realm);
        if (keycloak == null) {
            keycloak = createKcClient(realm);
            registry.put(realm, createKcClient(realm));
        }

        return keycloak;
    }

    private Keycloak createKcClient(String realm) {
        log.info("Creating keycloak admin client for realm {}", realm);
        KeycloakAdminClientsProperties.AdminClientProperties adminClientProperties = props.getAdminClients().get(realm);
        return KeycloakBuilder.builder()
                .serverUrl(props.getAuthServerUrl())
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(adminClientProperties.getClientId())
                .clientSecret(adminClientProperties.getClientSecret())
                .resteasyClient(
                        new ResteasyClientBuilder()
                                .connectionPoolSize(adminClientProperties.getPoolSize())
                                .connectTimeout(adminClientProperties.getTimeoutMs(), TimeUnit.MILLISECONDS)
                                .build()
                ).build();
    }

}
