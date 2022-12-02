package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.DepositStatus;

public class DepositStatusAdapter {
    @ToJson
    public String toJson(DepositStatus value) {
        return value.getValue();
    }

    @FromJson
    public DepositStatus fromJson(String s) {
        return DepositStatus.of(s);
    }
}
