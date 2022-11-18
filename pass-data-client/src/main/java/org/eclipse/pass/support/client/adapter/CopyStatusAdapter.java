package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.CopyStatus;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class CopyStatusAdapter {
    @ToJson
    public String toJson(CopyStatus value) {
        return value.getValue();
    }

    @FromJson
    public CopyStatus fromJson(String s) {
        return CopyStatus.of(s);
    }
}
