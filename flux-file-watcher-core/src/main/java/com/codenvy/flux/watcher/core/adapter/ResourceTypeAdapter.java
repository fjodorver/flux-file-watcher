package com.codenvy.flux.watcher.core.adapter;

import com.codenvy.flux.watcher.core.model.Resource;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ResourceTypeAdapter implements JsonSerializer<Resource>, JsonDeserializer<Resource> {
    private Gson gson = new GsonBuilder().registerTypeAdapter(byte[].class, new ByteArrayTypeAdapter()).create();

    @Override
    public JsonElement serialize(Resource resource, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = gson.toJsonTree(resource).getAsJsonObject();
        jsonObject.addProperty("path", jsonObject.get("resource").getAsString());
        return jsonObject;
    }

    @Override
    public Resource deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = element.getAsJsonObject();
        Resource resource = gson.fromJson(element, type);
        if (jsonObject.has("path"))
            resource.setPath(jsonObject.get("path").getAsString());
        return resource;
    }
}