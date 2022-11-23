package org.eclipse.pass.support.client.adapter;

import java.time.ZonedDateTime;

import org.eclipse.pass.support.client.Util;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class ZonedDateTimeAdapter {
    @ToJson
    public String toJson(ZonedDateTime value) {
        return value.format(Util.dateTimeFormatter());
    }

    @FromJson
    public ZonedDateTime fromJson(String s) {
        return ZonedDateTime.parse(s, Util.dateTimeFormatter());
    }
}