package org.eclipse.pass.support.client.adapter;

import org.eclipse.pass.support.client.model.EventType;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class EventTypeAdapter {
    @ToJson
    public String toJson(EventType value) {
        return value.getValue();
    }

    @FromJson
    public EventType fromJson(String s) {
        return EventType.of(s);
    }
}
