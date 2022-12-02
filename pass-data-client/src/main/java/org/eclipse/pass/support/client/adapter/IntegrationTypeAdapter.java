package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.IntegrationType;

public class IntegrationTypeAdapter {
    @ToJson
    public String toJson(IntegrationType value) {
        return value.getValue();
    }

    @FromJson
    public IntegrationType fromJson(String s) {
        return IntegrationType.of(s);
    }
}
