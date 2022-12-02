package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.Source;

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
