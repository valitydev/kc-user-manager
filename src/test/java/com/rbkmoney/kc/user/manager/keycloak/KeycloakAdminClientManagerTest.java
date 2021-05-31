package com.rbkmoney.kc.user.manager.keycloak;

import com.rbkmoney.kc.user.manager.config.properties.KeycloakAdminClientsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeycloakAdminClientManagerTest {

    private KeycloakAdminClientManager manager;

    @BeforeEach
    void setUp() {
        KeycloakAdminClientsProperties.AdminClientProperties master
                = new KeycloakAdminClientsProperties.AdminClientProperties();
        master.setClientId("master-client");
        master.setUsername("master-usr");
        master.setPassword("master-pwd");
        master.setPoolSize(1);

        KeycloakAdminClientsProperties.AdminClientProperties staging
                = new KeycloakAdminClientsProperties.AdminClientProperties();
        staging.setClientId("staging-client");
        staging.setUsername("staging-usr");
        staging.setPassword("staging-pwd");
        staging.setPoolSize(2);

        KeycloakAdminClientsProperties props = new KeycloakAdminClientsProperties();
        props.setAuthServerUrl("localhost:8080/auth");
        props.setAdminClients(Map.of("master", master, "staging", staging));

        manager = new KeycloakAdminClientManager(props);
    }

    @Test
    void getKcClient() {
        assertNotNull(manager.getKcClient("master"));
        assertNotNull(manager.getKcClient("staging"));
        assertThrows(IllegalArgumentException.class, () -> manager.getKcClient("wtf"));
    }
}
