// com/fasterxml/jackson/databind/deser/jdk/JDKScalarsTest.java
public void testVoidDirectDeser() throws Exception {
    Void v = MAPPER.readValue("null", Void.class);
    assertNull(v);
}
