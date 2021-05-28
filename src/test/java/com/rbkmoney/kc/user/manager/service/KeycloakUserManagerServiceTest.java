package com.rbkmoney.kc.user.manager.service;

import com.rbkmoney.kc.user.manager.keycloak.KeycloakAdminClientManager;
import com.rbkmoney.kc.user.manager.model.UserActions;
import com.rbkmoney.kc_user_manager.CreateUserResponse;
import com.rbkmoney.kc_user_manager.EmailSendingRequest;
import com.rbkmoney.kc_user_manager.KeycloakUserManagerException;
import com.rbkmoney.kc_user_manager.RedirectParams;
import com.rbkmoney.kc_user_manager.User;
import com.rbkmoney.kc_user_manager.UserID;
import org.apache.thrift.TException;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakUserManagerServiceTest {

    @Mock
    private KeycloakAdminClientManager keycloakAdminClientManager;

    @Mock
    private Keycloak adminClient;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    private KeycloakUserManagerService service;

    private static final String EMAIL = "email";
    private static final String REALM = "realm";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String CREATED_USER_RESOURCE = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";

    @BeforeEach
    void setUp() {
        service = new KeycloakUserManagerService(keycloakAdminClientManager);
    }

    @Test
    void create() throws TException {
        User user = new User();
        UserID userID = new UserID();
        userID.setEmail(EMAIL);
        userID.setRealm(REALM);
        user.setUserId(userID);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);

        mockAdminClientAndToken();
        mockRealmAndUsersResource();

        Headers<Object> headers = new Headers<>();
        headers.put("Location", Collections.singletonList(CREATED_USER_RESOURCE));
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(new ServerResponse(null, 201, headers))
                .thenReturn(new ServerResponse(null, 409, null));

        CreateUserResponse createUserResponse = service.create(user);
        assertEquals(CREATED_USER_RESOURCE, createUserResponse.getStatus().getSuccess().getId());
        assertThrows(KeycloakUserManagerException.class, () -> service.create(user));
        verify(keycloakAdminClientManager, times(2)).getKcClient(any());
        verify(usersResource, times(2)).create(any());
    }

    @Test
    void sendUpdatePasswordEmail() throws TException {
        mockAdminClientAndToken();
        mockRealmAndUsersResource();

        UserRepresentation user = createUser(EMAIL);

        List<UserRepresentation> noRequiredUserList = Arrays.asList(
                createUser("stub_1"),
                createUser("stub_2")
        );
        List<UserRepresentation> withRequiredUserList = Arrays.asList(
                user,
                createUser("stub_1"),
                createUser("stub_2")
        );

        when(usersResource.search(
                null,
                null,
                null,
                EMAIL,
                null,
                null,
                true)
        )
                .thenReturn(new ArrayList<>())
                .thenReturn(noRequiredUserList)
                .thenReturn(withRequiredUserList);

        when(usersResource.get(user.getId())).thenReturn(userResource);

        UserID userID = new UserID();
        userID.setEmail(EMAIL);
        userID.setRealm(REALM);
        RedirectParams redirectParams = new RedirectParams();
        redirectParams.setClientId(CLIENT_ID);
        redirectParams.setRedirectUri(REDIRECT_URI);
        EmailSendingRequest rq = new EmailSendingRequest();
        rq.setUserId(userID);

        assertThrows(KeycloakUserManagerException.class, () -> service.sendUpdatePasswordEmail(rq));
        assertThrows(KeycloakUserManagerException.class, () -> service.sendUpdatePasswordEmail(rq));
        service.sendUpdatePasswordEmail(rq);
        rq.setRedirectParams(redirectParams);
        service.sendUpdatePasswordEmail(rq);

        verify(usersResource, times(4))
                .search(
                        null,
                        null,
                        null,
                        EMAIL,
                        null,
                        null,
                        true
                );
        verify(usersResource, times(2))
                .get(user.getId());

        ArgumentCaptor<String> clientIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> redirectUriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> actionsCaptor = ArgumentCaptor.forClass(List.class);

        verify(userResource, times(2))
                .executeActionsEmail(clientIdCaptor.capture(), redirectUriCaptor.capture(), actionsCaptor.capture());

        assertEquals(2, clientIdCaptor.getAllValues().size());
        assertNull(clientIdCaptor.getAllValues().get(0));
        assertEquals(CLIENT_ID, clientIdCaptor.getAllValues().get(1));

        assertEquals(2, redirectUriCaptor.getAllValues().size());
        assertNull(redirectUriCaptor.getAllValues().get(0));
        assertEquals(REDIRECT_URI, redirectUriCaptor.getAllValues().get(1));

        assertEquals(2, actionsCaptor.getAllValues().size());
        actionsCaptor.getAllValues().forEach(actions -> {
            assertEquals(1, actions.size());
            assertEquals(UserActions.UPDATE_PASSWORD.name(), actions.get(0));
        });
    }

    @Test
    void sendVerifyUserEmail() throws TException {
        mockAdminClientAndToken();
        mockRealmAndUsersResource();

        UserRepresentation user = createUser(EMAIL);

        List<UserRepresentation> noRequiredUserList = Arrays.asList(
                createUser("stub_1"),
                createUser("stub_2")
        );
        List<UserRepresentation> withRequiredUserList = Arrays.asList(
                user,
                createUser("stub_1"),
                createUser("stub_2")
        );

        when(usersResource.search(
                null,
                null,
                null,
                EMAIL,
                null,
                null,
                true)
        )
                .thenReturn(new ArrayList<>())
                .thenReturn(noRequiredUserList)
                .thenReturn(withRequiredUserList);

        when(usersResource.get(user.getId())).thenReturn(userResource);

        UserID userID = new UserID();
        userID.setEmail(EMAIL);
        userID.setRealm(REALM);

        RedirectParams redirectParams = new RedirectParams();
        redirectParams.setClientId(CLIENT_ID);
        redirectParams.setRedirectUri(REDIRECT_URI);
        EmailSendingRequest rq = new EmailSendingRequest();
        rq.setUserId(userID);

        assertThrows(KeycloakUserManagerException.class, () -> service.sendVerifyUserEmail(rq));
        assertThrows(KeycloakUserManagerException.class, () -> service.sendVerifyUserEmail(rq));
        service.sendVerifyUserEmail(rq);
        rq.setRedirectParams(redirectParams);
        service.sendVerifyUserEmail(rq);

        verify(usersResource, times(4))
                .search(
                        null,
                        null,
                        null,
                        EMAIL,
                        null,
                        null,
                        true
                );
        verify(usersResource, times(2))
                .get(user.getId());

        ArgumentCaptor<String> clientIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> redirectUriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> actionsCaptor = ArgumentCaptor.forClass(List.class);

        verify(userResource, times(2))
                .executeActionsEmail(clientIdCaptor.capture(), redirectUriCaptor.capture(), actionsCaptor.capture());

        assertEquals(2, clientIdCaptor.getAllValues().size());
        assertNull(clientIdCaptor.getAllValues().get(0));
        assertEquals(CLIENT_ID, clientIdCaptor.getAllValues().get(1));

        assertEquals(2, redirectUriCaptor.getAllValues().size());
        assertNull(redirectUriCaptor.getAllValues().get(0));
        assertEquals(REDIRECT_URI, redirectUriCaptor.getAllValues().get(1));

        assertEquals(2, actionsCaptor.getAllValues().size());
        actionsCaptor.getAllValues().forEach(actions -> {
            assertEquals(1, actions.size());
            assertEquals(UserActions.VERIFY_EMAIL.name(), actions.get(0));
        });
    }

    private void mockAdminClientAndToken() {
        when(keycloakAdminClientManager.getKcClient(REALM)).thenReturn(adminClient);
        when(adminClient.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessToken()).thenReturn(new AccessTokenResponse());
    }

    private void mockRealmAndUsersResource() {
        when(adminClient.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    private UserRepresentation createUser(String email) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(UUID.randomUUID().toString());
        userRepresentation.setEmail(email);
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        return userRepresentation;
    }
}
