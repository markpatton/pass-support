package org.eclipse.pass.support.client.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import org.eclipse.pass.support.client.model.FileRole;

public class FileRoleAdapter {
    @ToJson
    public String toJson(FileRole value) {
        return value.getValue();
    }

    @FromJson
    public FileRole fromJson(String s) {
        return FileRole.of(s);
    }
}
