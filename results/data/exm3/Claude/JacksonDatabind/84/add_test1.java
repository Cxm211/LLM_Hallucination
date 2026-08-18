// com/fasterxml/jackson/databind/type/TestTypeFactoryWithRecursiveTypes.java
public void testSubPropertiesIncludedWhenSerializingSubWhenSubTypeLoadedMultipleTimes() throws IOException {
    TypeFactory tf = TypeFactory.defaultInstance();
    tf.constructType(Sub.class);
    tf.constructType(Sub.class);
    Sub sub = new Sub();
    String serialized = objectMapper().writeValueAsString(sub);
    assertEquals("{\"base\":1,\"sub\":2}", serialized);
}