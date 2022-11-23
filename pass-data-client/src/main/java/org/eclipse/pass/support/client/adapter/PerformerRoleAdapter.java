package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.PerformerRole;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class PerformerRoleAdapter {
    @ToJson
    public String toJson(PerformerRole value) {
        return value.getValue();
    }

    @FromJson
    public PerformerRole fromJson(String s) {
        return PerformerRole.of(s);
    }
}
