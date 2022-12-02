package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.EventType;

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
