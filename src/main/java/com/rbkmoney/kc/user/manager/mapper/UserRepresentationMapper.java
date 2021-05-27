package com.rbkmoney.kc.user.manager.mapper;

import com.rbkmoney.kc_user_manager.User;
import org.keycloak.representations.idm.UserRepresentation;

public class UserRepresentationMapper {

    private UserRepresentationMapper() {
    }

    public static UserRepresentation map(User user) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(user.getUserId().getEmail());
        kcUser.setEmail(user.getUserId().getEmail());
        kcUser.setFirstName(user.getFirstName());
        kcUser.setLastName(user.getLastName());
        kcUser.setEmailVerified(true);
        kcUser.setEnabled(true);

        return kcUser;
    }

}
