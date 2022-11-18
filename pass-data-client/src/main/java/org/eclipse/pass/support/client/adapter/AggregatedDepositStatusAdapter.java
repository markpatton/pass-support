package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.AggregatedDepositStatus;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class AggregatedDepositStatusAdapter {
    @ToJson
    public String toJson(AggregatedDepositStatus value) {
        return value.getValue();
    }

    @FromJson
    public AggregatedDepositStatus fromJson(String s) {
        return AggregatedDepositStatus.of(s);
    }
}
