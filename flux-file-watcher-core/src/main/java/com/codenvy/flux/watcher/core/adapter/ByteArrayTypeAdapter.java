package com.codenvy.flux.watcher.core.adapter;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ByteArrayTypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    @Override
    public byte[] deserialize(JsonElement element, Type type, JsonDeserializationContext content) throws JsonParseException {
        return element.getAsString().getBytes();
    }

    @Override
    public JsonElement serialize(byte[] bytes, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(new String(bytes));
    }
}