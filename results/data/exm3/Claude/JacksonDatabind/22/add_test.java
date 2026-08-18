// com/fasterxml/jackson/databind/ser/TestJsonValue.java
public void testCustomSerializerForMapLikeType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(new JsonSerializer<java.util.AbstractMap>() {
                @Override
                public void serialize(java.util.AbstractMap value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString("custom-map");
                }
                @Override
                public Class<java.util.AbstractMap> handledType() {
                    return java.util.AbstractMap.class;
                }
            })
        );
        
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("key", "value");
        assertEquals(quote("custom-map"), mapper.writeValueAsString(map));
    }