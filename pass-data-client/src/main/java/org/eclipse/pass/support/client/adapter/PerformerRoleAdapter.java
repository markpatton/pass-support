package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.PerformerRole;

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
