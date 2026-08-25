package com.tianxin.platform.system.store;

import com.tianxin.platform.system.model.SystemRole;
import com.tianxin.platform.system.model.SystemUser;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IdentityStore {
    Optional<SystemUser> findUserByUsername(String username);
    Optional<SystemUser> findUserById(UUID id);
    List<SystemUser> listUsers();
    List<SystemRole> listRoles();
    SystemUser createUser(String username, String displayName, String password, Set<String> roleCodes);
    void updateUserStatus(UUID id, boolean enabled);
}
