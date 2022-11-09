package org.eclipse.pass.support.client.adapter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class ZonedDateTimeAdapter {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    @ToJson
    public String toJson(ZonedDateTime value) {
        return value.format(FORMATTER);
    }

    @FromJson
    public ZonedDateTime fromJson(String s) {
        return ZonedDateTime.parse(s, FORMATTER);
    }
}
