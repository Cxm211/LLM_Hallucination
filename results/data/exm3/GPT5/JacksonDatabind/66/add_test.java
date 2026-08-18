// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java::testEnumKeyDeserializerReceivesStringToken
public void testEnumKeyDeserializerReceivesStringToken() throws Exception {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                assertEquals(JsonToken.VALUE_STRING, p.currentToken());
                return SuperTypeEnum.valueOf(p.getText());
            }
        });
        ObjectMapper mapper = new ObjectMapper().registerModule(simpleModule);
        SuperType superType = mapper.readValue("{\"someMap\": {\"FOO\": \"baz\"}}", SuperType.class);
        assertEquals("baz", superType.someMap.get(SuperTypeEnum.FOO));
    }