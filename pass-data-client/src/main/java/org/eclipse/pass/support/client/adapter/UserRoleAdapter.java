package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.UserRole;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class UserRoleAdapter {
    @ToJson
    public String toJson(UserRole value) {
        return value.getValue();
    }

    @FromJson
    public UserRole fromJson(String s) {
        return UserRole.of(s);
    }
}
