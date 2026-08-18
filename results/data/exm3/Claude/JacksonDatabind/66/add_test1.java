// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testCustomEnumKeyDeserializerWithMultipleKeys() throws IOException
{
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
        @Override
        public SuperTypeEnum deserialize(JsonParser p, DeserializationContext deserializationContext)
                throws IOException
        {
            return SuperTypeEnum.valueOf(p.getText());
        }
    });
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(simpleModule);

    SuperType superType = mapper.readValue("{\"someMap\": {\"FOO\": \"value1\", \"BAR\": \"value2\"}}",
            SuperType.class);
    assertEquals("Deserialized someMap.FOO should equal value1", "value1",
            superType.someMap.get(SuperTypeEnum.FOO));
    assertEquals("Deserialized someMap.BAR should equal value2", "value2",
            superType.someMap.get(SuperTypeEnum.BAR));
}