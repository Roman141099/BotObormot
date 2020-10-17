package Main.UBot.com;

import com.google.gson.*;

import java.lang.reflect.Type;

class SessionDeserializer implements JsonSerializer<Object> , JsonDeserializer<Object> {
    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement jsonElement = context.serialize(src, src.getClass());
        jsonElement.getAsJsonObject().addProperty("Session type endpoint", src.getClass().getCanonicalName());
        return jsonElement;
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get("Session type endpoint").getAsString();
        try {
            Class<?> clz = Class.forName(className);
            return context.deserialize(json, clz);
        } catch (ClassNotFoundException e) {
            return new ClassNotFoundException("Can't find provided class");
        }
    }
}


