package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.SubmissionStatus;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class SubmissionStatusAdapter {
    @ToJson
    public String toJson(SubmissionStatus value) {
        return value.getValue();
    }

    @FromJson
    public SubmissionStatus fromJson(String s) {
        return SubmissionStatus.of(s);
    }
}
