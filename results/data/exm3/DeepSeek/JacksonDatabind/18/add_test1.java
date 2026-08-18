// com/fasterxml/jackson/databind/seq/ReadRecoveryTest.java
public void testSingleElementArray() throws Exception {
    final String JSON = aposToQuotes("[{'a':3}]");
    MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
    assertTrue(it.hasNextValue());
    Bean bean = it.nextValue();
    assertEquals(3, bean.a);
    assertFalse(it.hasNextValue());
    it.close();
}
