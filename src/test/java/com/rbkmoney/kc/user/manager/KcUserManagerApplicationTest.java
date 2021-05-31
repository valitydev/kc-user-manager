package com.rbkmoney.kc.user.manager;

import com.rbkmoney.kc.user.manager.keycloak.KeycloakAdminClientManager;
import com.rbkmoney.kc_user_manager.CreateUserResponse;
import com.rbkmoney.kc_user_manager.KeycloakUserManagerSrv;
import com.rbkmoney.kc_user_manager.User;
import com.rbkmoney.kc_user_manager.UserID;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.rbkmoney.kc.user.manager.util.Constants.CREATED_USER_RESOURCE;
import static com.rbkmoney.kc.user.manager.util.Constants.EMAIL;
import static com.rbkmoney.kc.user.manager.util.Constants.REALM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = KcUserManagerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class KcUserManagerApplicationTest {

    @MockBean
    private KeycloakAdminClientManager keycloakAdminClientManager;

    @Mock
    private Keycloak adminClient;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @LocalServerPort
    private int port;

    @Test
    void servletTest() throws TException, URISyntaxException {
        when(keycloakAdminClientManager.getKcClient(REALM)).thenReturn(adminClient);
        when(adminClient.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessToken()).thenReturn(new AccessTokenResponse());
        when(adminClient.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        Headers<Object> headers = new Headers<>();
        headers.put("Location", Collections.singletonList(CREATED_USER_RESOURCE));
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(new ServerResponse(null, 201, headers));

        UserID userID = new UserID();
        userID.setRealm(REALM);
        userID.setEmail(EMAIL);
        User user = new User();
        user.setUserId(userID);

        KeycloakUserManagerSrv.Iface client = new THSpawnClientBuilder()
                .withNetworkTimeout(0)
                .withAddress(new URI("http://localhost:" + port + "/v1/keycloak/users"))
                .build(KeycloakUserManagerSrv.Iface.class);

        CreateUserResponse createUserResponse = client.create(user);
        assertEquals(CREATED_USER_RESOURCE, createUserResponse.getStatus().getSuccess().getId());
    }
}
