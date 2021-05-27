package com.rbkmoney.kc.user.manager.mapper;

import com.rbkmoney.kc_user_manager.User;
import com.rbkmoney.kc_user_manager.UserID;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepresentationMapperTest {

    private static final String EMAIL = "email";
    private static final String REALM = "realm";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";

    @Test
    void map() {
        User user = new User();
        UserID userID = new UserID();
        userID.setEmail(EMAIL);
        userID.setRealm(REALM);
        user.setUserId(userID);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);

        UserRepresentation actual = UserRepresentationMapper.map(user);

        assertEquals(EMAIL, actual.getUsername());
        assertEquals(EMAIL, actual.getEmail());
        assertEquals(FIRST_NAME, actual.getFirstName());
        assertEquals(LAST_NAME, actual.getLastName());
        assertTrue(actual.isEnabled());
        assertTrue(actual.isEmailVerified());
    }
}
