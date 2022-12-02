package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.ContributorRole;

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
