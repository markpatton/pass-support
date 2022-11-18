package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.AwardStatus;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class AwardStatusAdapter {
    @ToJson
    public String toJson(AwardStatus value) {
        return value.getValue();
    }

    @FromJson
    public AwardStatus fromJson(String s) {
        return AwardStatus.of(s);
    }
}
