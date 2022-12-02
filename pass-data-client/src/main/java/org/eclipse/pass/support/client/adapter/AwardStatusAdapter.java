package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.AwardStatus;

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
