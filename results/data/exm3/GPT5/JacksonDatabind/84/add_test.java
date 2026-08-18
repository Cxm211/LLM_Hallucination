// com/fasterxml/jackson/databind/type/TestTypeFactoryWithRecursiveTypes.java::testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedBeforeBaseType
public void testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedBeforeBaseType() throws IOException {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(Sub.class);
        tf.constructType(Base.class);
        Sub sub = new Sub();
        String serialized = objectMapper().writeValueAsString(sub);
        assertEquals("{\"base\":1,\"sub\":2}", serialized);
    }