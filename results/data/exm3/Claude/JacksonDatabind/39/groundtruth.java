// ===== FIXED com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer :: deserialize(JsonParser, DeserializationContext) [lines 31-47] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-39-fixed/src/main/java/com/fasterxml/jackson/databind/deser/std/NullifyingDeserializer.java =====
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // 29-Jan-2016, tatu: Simple skipping for all other tokens, but FIELD_NAME bit
        //    special unfortunately
        if (p.hasToken(JsonToken.FIELD_NAME)) {
            while (true) {
                JsonToken t = p.nextToken();
                if ((t == null) || (t == JsonToken.END_OBJECT)) {
                    break;
                }
                p.skipChildren();
            }
        } else {
            p.skipChildren();
        }
        return null;
    }
