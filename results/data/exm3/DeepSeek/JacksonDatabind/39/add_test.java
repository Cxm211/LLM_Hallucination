// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testNullifyingDeserializerScalar() throws Exception {
    com.fasterxml.jackson.core.JsonFactory f = new com.fasterxml.jackson.core.JsonFactory();
    com.fasterxml.jackson.core.JsonParser p = f.createParser("123");
    p.nextToken();
    com.fasterxml.jackson.databind.deser.impl.NullifyingDeserializer nd = com.fasterxml.jackson.databind.deser.impl.NullifyingDeserializer.instance;
    Object result = nd.deserialize(p, null);
    assertNull(result);
    assertNull(p.nextToken());
}
