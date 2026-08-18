// com/fasterxml/jackson/databind/ser/TestJsonValue.java
public void testJsonValueWithCustomOverrideForCollection() throws Exception
    {
        final MyList INPUT = new MyList();
        INPUT.add("foo");
        // by default, @JsonValue should be used
        assertEquals(quote("value"), MAPPER.writeValueAsString(INPUT));
        // but custom serializer should override it
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(MyList.class, new MyListSerializer())
            );
        assertEquals("42", mapper.writeValueAsString(INPUT));
    }
    
    static class MyList extends ArrayList<String> {
        @JsonValue
        public String getValue() {
            return "value";
        }
    }
    
    static class MyListSerializer extends JsonSerializer<MyList> {
        @Override
        public void serialize(MyList value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(42);
        }
    }
