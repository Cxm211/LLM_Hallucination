// com/fasterxml/jackson/databind/deser/jdk/JDKScalarsTest.java::testVoidDeser
public void testVoidRootDeser() throws Exception
    {
        Void v = MAPPER.readValue("123", Void.class);
        assertNull(v);
    }