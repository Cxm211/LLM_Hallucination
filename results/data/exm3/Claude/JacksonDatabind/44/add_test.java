// com/fasterxml/jackson/databind/jsontype/TestSubtypes.java
public void testNarrowWithSameClass() throws Exception {
    Issue1125Wrapper result = MAPPER.readValue(aposToQuotes("{'value':{'a':1,'def':2,'b':3}}"),
            Issue1125Wrapper.class);
    assertNotNull(result.value);
    assertEquals(Default1125.class, result.value.getClass());
}