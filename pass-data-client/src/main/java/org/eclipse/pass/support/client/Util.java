package org.eclipse.pass.support.client;

import java.time.format.DateTimeFormatter;

public class Util {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /**
     * The ZonedDateTime fields in the model must use this formatter.
     *
     * @return formatter
     */
    public static DateTimeFormatter dateTimeFormatter() {
        return FORMATTER;
    }
}