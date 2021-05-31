package com.rbkmoney.kc.user.manager.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakAdminClientsProperties {

    private String authServerUrl;
    private Map<String, AdminClientProperties> adminClients;

    @Getter
    @Setter
    public static class AdminClientProperties {
        private String clientId;
        private String username;
        private String password;
        private Integer poolSize;
    }
}
