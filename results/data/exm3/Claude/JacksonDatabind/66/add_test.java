// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testCustomEnumKeyDeserializerWithNullKey() throws IOException
{
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
        @Override
        public SuperTypeEnum deserialize(JsonParser p, DeserializationContext deserializationContext)
                throws IOException
        {
            String text = p.getText();
            if (text == null) {
                return null;
            }
            return SuperTypeEnum.valueOf(text);
        }
    });
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(simpleModule);

    SuperType superType = mapper.readValue("{\"someMap\": {\"BAR\": \"baz\"}}",
            SuperType.class);
    assertEquals("Deserialized someMap.BAR should equal baz", "baz",
            superType.someMap.get(SuperTypeEnum.BAR));
}