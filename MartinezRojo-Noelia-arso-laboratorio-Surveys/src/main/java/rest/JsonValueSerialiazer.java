package rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import javax.json.JsonValue;

public class JsonValueSerialiazer extends StdSerializer<JsonValue> {
	
    public JsonValueSerialiazer() {
        this(null);
    }
  
    public JsonValueSerialiazer(Class<JsonValue> t) {
        super(t);
    }
 
    @Override
    public void serialize(JsonValue value, JsonGenerator jgen, SerializerProvider provider) 
      throws IOException, JsonProcessingException {
 
        jgen.writeStartObject();
        jgen.writeStringField("op", value.asJsonObject().getString("op"));
        jgen.writeStringField("path", value.asJsonObject().getString("path"));
        jgen.writeObjectField("value", value.asJsonObject().get("value"));
        jgen.writeEndObject();
    }
}
