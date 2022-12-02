package org.eclipse.pass.support.client.adapter;

import java.net.URI;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class UriAdapter {
    @ToJson
    public String toJson(URI value) {
        return value.toString();
    }

    @FromJson
    public URI fromJson(String s) {
        return URI.create(s);
    }
}
