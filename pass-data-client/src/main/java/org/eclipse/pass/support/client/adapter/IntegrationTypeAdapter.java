package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.IntegrationType;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

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
