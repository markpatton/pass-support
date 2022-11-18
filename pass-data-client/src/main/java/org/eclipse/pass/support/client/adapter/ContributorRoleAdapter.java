package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.ContributorRole;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class ContributorRoleAdapter {
    @ToJson
    public String toJson(ContributorRole value) {
        return value.getValue();
    }

    @FromJson
    public ContributorRole fromJson(String s) {
        return ContributorRole.of(s);
    }
}
