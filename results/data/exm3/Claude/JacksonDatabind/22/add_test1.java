// com/fasterxml/jackson/databind/ser/TestJsonValue.java
public void testCustomSerializerForCollectionLikeType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(new JsonSerializer<java.util.AbstractCollection>() {
                @Override
                public void serialize(java.util.AbstractCollection value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString("custom-collection");
                }
                @Override
                public Class<java.util.AbstractCollection> handledType() {
                    return java.util.AbstractCollection.class;
                }
            })
        );
        
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("item");
        assertEquals(quote("custom-collection"), mapper.writeValueAsString(list));
    }