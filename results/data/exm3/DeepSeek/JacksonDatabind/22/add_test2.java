// com/fasterxml/jackson/databind/ser/TestJsonValue.java
public void testJsonValueWithCustomOverrideForAnotherPojo() throws Exception
    {
        final Bean839 INPUT = new Bean839();
        // by default, @JsonValue should be used
        assertEquals(quote("bean839"), MAPPER.writeValueAsString(INPUT));
        // but custom serializer should override it
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(Bean839.class, new Bean839Serializer())
            );
        assertEquals("123", mapper.writeValueAsString(INPUT));
    }
    
    static class Bean839 {
        @JsonValue
        public String getVal() {
            return "bean839";
        }
    }
    
    static class Bean839Serializer extends JsonSerializer<Bean839> {
        @Override
        public void serialize(Bean839 value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(123);
        }
    }
