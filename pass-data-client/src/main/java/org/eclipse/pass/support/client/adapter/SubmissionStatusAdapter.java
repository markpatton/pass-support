package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.SubmissionStatus;

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
