// com/fasterxml/jackson/databind/type/TestTypeFactoryWithRecursiveTypes.java
public void testBasePropertiesIncludedWhenSerializingBaseWhenBaseTypeLoadedMultipleTimes() throws IOException {
    TypeFactory tf = TypeFactory.defaultInstance();
    tf.constructType(Base.class);
    tf.constructType(Base.class);
    Base base = new Base();
    String serialized = objectMapper().writeValueAsString(base);
    assertEquals("{\"base\":1}", serialized);
}