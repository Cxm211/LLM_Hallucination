// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testNullifyingDeserializerFieldName() throws Exception {
    com.fasterxml.jackson.core.JsonFactory f = new com.fasterxml.jackson.core.JsonFactory();
    com.fasterxml.jackson.core.JsonParser p = f.createParser("{\"field\": \"value\"}");
    p.nextToken();
    p.nextToken();
    com.fasterxml.jackson.databind.deser.impl.NullifyingDeserializer nd = com.fasterxml.jackson.databind.deser.impl.NullifyingDeserializer.instance;
    Object result = nd.deserialize(p, null);
    assertNull(result);
    assertEquals(com.fasterxml.jackson.core.JsonToken.END_OBJECT, p.nextToken());
    assertNull(p.nextToken());
}
