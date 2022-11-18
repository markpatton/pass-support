package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.Source;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class SourceAdapter {
    @ToJson
    public String toJson(Source value) {
        return value.getValue();
    }

    @FromJson
    public Source fromJson(String s) {
        return Source.of(s);
    }
}
