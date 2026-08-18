// com/fasterxml/jackson/databind/jsontype/TestSubtypes.java
public void testNarrowWithDifferentSubclass() throws Exception {
    Issue1125Wrapper result = MAPPER.readValue(aposToQuotes("{'value':{'a':10,'def':20,'b':30}}"),
            Issue1125Wrapper.class);
    assertNotNull(result.value);
    assertTrue(result.value instanceof Base1125);
    Default1125 impl = (Default1125) result.value;
    assertEquals(10, impl.a);
    assertEquals(30, impl.b);
    assertEquals(20, impl.def);
}