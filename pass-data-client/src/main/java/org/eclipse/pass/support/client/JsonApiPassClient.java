package org.eclipse.pass.support.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.pass.support.client.adapter.UriAdapter;
import org.eclipse.pass.support.client.adapter.UserRoleAdapter;
import org.eclipse.pass.support.client.adapter.ZonedDateTimeAdapter;
import org.eclipse.pass.support.client.model.Contributor;
import org.eclipse.pass.support.client.model.Deposit;
import org.eclipse.pass.support.client.model.File;
import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.Journal;
import org.eclipse.pass.support.client.model.PassEntity;
import org.eclipse.pass.support.client.model.Policy;
import org.eclipse.pass.support.client.model.Publication;
import org.eclipse.pass.support.client.model.Publisher;
import org.eclipse.pass.support.client.model.Repository;
import org.eclipse.pass.support.client.model.RepositoryCopy;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.eclipse.pass.support.client.model.User;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonAdapter.Factory;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonReader.Token;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import jsonapi.Document;
import jsonapi.Document.IncludedSerialization;
import jsonapi.JsonApiFactory;
import jsonapi.ResourceObject;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class JsonApiPassClient implements PassClient {
    private final static String JSON_API_CONTENT_TYPE = "application/vnd.api+json";
    private final static MediaType JSON_API_MEDIA_TYPE = MediaType.parse("application/vnd.api+json; charset=utf-8");

    private final Moshi moshi;
    private final String baseUrl;
    private final OkHttpClient client;

    public JsonApiPassClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.client = new OkHttpClient();

        Factory factory = new JsonApiFactory.Builder().addTypes(Contributor.class, Deposit.class, File.class,
                Funder.class, Grant.class, Journal.class, Policy.class, Publication.class, Publisher.class,
                Repository.class, RepositoryCopy.class, Submission.class, SubmissionEvent.class, User.class).build();

        this.moshi = new Moshi.Builder().add(factory).add(new ZonedDateTimeAdapter()).add(new UriAdapter())
                .add(new UserRoleAdapter()).build();
    }

    private String get_url(PassEntity obj) {
        return get_url(obj.getClass(), obj.getId());
    }

    private String get_url(Class<?> type, String id) {
        String name = get_json_type(type);

        if (id == null) {
            return baseUrl + name;
        } else {
            return baseUrl + name + "/" + id;
        }
    }

    private String get_json_type(Class<?> type) {
        String name = type.getSimpleName();

        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);

        return new String(chars);
    }

    @Override
    public <T extends PassEntity> void createObject(T obj) throws IOException {
        JsonAdapter<Document<T>> adapter = moshi.adapter(Types.newParameterizedType(Document.class, obj.getClass()));

        Document<T> doc = Document.with(obj).includedSerialization(IncludedSerialization.NONE).build();

        String json = adapter.toJson(doc);

        System.err.println("POST: " + json);

        String url = get_url(obj);
        RequestBody body = RequestBody.create(json, JSON_API_MEDIA_TYPE);
        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException(
                    "Create failed: " + url + " returned " + response.code() + " " + response.body().string());
        }

        Document<T> result_doc = adapter.fromJson(response.body().string());
        obj.setId(result_doc.requireData().getId());
    }

    @Override
    public <T extends PassEntity> void updateObject(T obj) throws IOException {
        JsonAdapter<Document<T>> adapter = moshi.adapter(Types.newParameterizedType(Document.class, obj.getClass()));
        Document<T> doc = Document.with(obj).includedSerialization(IncludedSerialization.NONE).build();

        String json = adapter.toJson(doc);

        System.err.println("POST: " + json);

        String url = get_url(obj);
        RequestBody body = RequestBody.create(json, JSON_API_MEDIA_TYPE);
        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException(
                    "Update failed: " + url + " returned " + response.code() + " " + response.body().string());
        }
    }

    private static class Target {
        List<String> ids;
        String type;
        boolean many;

        Target() {
            this.ids = new ArrayList<>();
        }
    }

    private Map<String, Target> get_relationships(String json) throws IOException {
        Map<String, Target> result = new HashMap<>();

        try (Buffer buffer = new Buffer(); JsonReader reader = JsonReader.of(buffer.writeUtf8(json))) {
            reader.beginObject();

            while (reader.hasNext()) {
                if (reader.nextName().equals("data")) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        if (reader.nextName().equals("relationships")) {
                            reader.beginObject();

                            while (reader.hasNext()) {
                                String rel_name = reader.nextName();

                                reader.beginObject();
                                while (reader.hasNext()) {
                                    if (reader.nextName().equals("data")) {
                                        Token next = reader.peek();

                                        Target target = new Target();

                                        if (next == Token.BEGIN_ARRAY) {
                                            reader.beginArray();
                                            target.many = true;

                                            while (reader.hasNext()) {
                                                fill_target(target, reader);
                                            }

                                            reader.endArray();
                                        } else if (next == Token.BEGIN_OBJECT) {
                                            target.many = false;
                                            fill_target(target, reader);
                                        } else {
                                            reader.skipValue();
                                        }

                                        if (target.ids.size() > 0) {
                                            result.put(rel_name, target);
                                        }
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.endObject();
                            }

                            reader.endObject();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
        }

        return result;
    }

    private void fill_target(Target target, JsonReader reader) throws IOException {
        reader.beginObject();

        String id = null;
        String type = null;

        while (reader.hasNext()) {
            switch (reader.nextName()) {
            case "id":
                id = reader.nextString();
                break;

            case "type":
                type = reader.nextString();
                break;

            default:
                reader.skipValue();
            }
        }

        if (id != null && type != null) {
            target.ids.add(id);
            target.type = type;
        }

        reader.endObject();
    }

    private void set_relationship(Object obj, String name, Target target) throws IOException {
        try {
            Object target_obj = Class.forName(target.type).getConstructor(String.class).newInstance("");

            Method m = obj.getClass().getMethod("set" + name, target_obj.getClass());
            m.invoke(obj, target_obj);

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private void setRelationship(Object obj, String name, List<Target> targets) {

        // Method m = obj.getClass().getMethod("setId");

        // Class.forName("").getConstructor(null).newInstance(null);

    }

    @Override
    public <T extends PassEntity> T getObject(Class<T> type, String id, String... include) throws IOException {
        JsonAdapter<Document<T>> adapter = moshi.adapter(Types.newParameterizedType(Document.class, type));

        HttpUrl.Builder url_builder = HttpUrl.parse(get_url(type, id)).newBuilder();
        if (include != null && include.length > 0) {
            url_builder.addQueryParameter("include", String.join(",", include));
        }
        HttpUrl url = url_builder.build();

        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).get().build();

        Response response = client.newCall(request).execute();

        if (response.code() == 404) {
            return null;
        }

        String body = response.body().string();

        System.err.println("GET: " + body);

        if (!response.isSuccessful()) {
            throw new IOException("Get failed: " + url + " returned " + response.code() + " " + body);
        }

        Document<T> doc = adapter.fromJson(body);
        T result = doc.requireData();

        Map<String, Target> rels = get_relationships(body);


        rels.forEach((rel_name, rel_target) -> {
            System.err.println(rel_name);
            System.err.println(rel_target.type);
            System.err.println(rel_target.many);
        });
        System.err.println("Moo");



        return result;
    }

    @Override
    public <T extends PassEntity> void deleteObject(Class<T> type, String id) throws IOException {
        String url = get_url(type, id);

        Request request = new Request.Builder().url(url).delete().build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException(
                    "Delete failed: " + url + " returned " + response.code() + " " + response.body().string());
        }
    }

    @Override
    public <T extends PassEntity> PassClientResult<T> selectObjects(PassClientSelector<T> selector) throws IOException {
        JsonAdapter<Document<List<T>>> adapter = moshi.adapter(
                Types.newParameterizedType(Document.class, Types.newParameterizedType(List.class, selector.getType())));
        HttpUrl.Builder url_builder = HttpUrl.parse(get_url(selector.getType(), null)).newBuilder();

        String[] include = selector.getInclude();
        if (include != null && include.length > 0) {
            url_builder.addQueryParameter("include", String.join(",", include));
        }

        if (selector.getFilter() != null) {
            url_builder.addQueryParameter("filter", selector.getFilter());
        }

        if (selector.getSorting() != null) {
            url_builder.addQueryParameter("sort", selector.getSorting());
        }

        url_builder.addQueryParameter("page[offset]", "" + selector.getOffset());
        url_builder.addQueryParameter("page[limit]", "" + selector.getLimit());
        url_builder.addQueryParameter("page[totals]", null);

        HttpUrl url = url_builder.build();

        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).get().build();

        Response response = client.newCall(request).execute();

        if (response.code() == 404) {
            return null;
        }

        String body = response.body().string();

        System.err.println("GET: " + body);

        if (!response.isSuccessful()) {
            throw new IOException("Select failed: " + url + " returned " + response.code() + " " + body);
        }

        Document<List<T>> doc = adapter.fromJson(body);
        List<T> matches = doc.requireData();
        long total = -1;

        if (doc.getMeta().has("page")) {
            Map<?, ?> page = (Map<?, ?>) doc.getMeta().get("page");

            if (page.containsKey("totalRecords")) {
                total = ((Double) page.get("totalRecords")).longValue();
            }
        }

        return new PassClientResult<>(matches, total);
    }
}
