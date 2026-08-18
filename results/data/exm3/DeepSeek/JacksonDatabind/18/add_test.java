// com/fasterxml/jackson/databind/seq/ReadRecoveryTest.java
public void testEmptyArray() throws Exception {
    final String JSON = aposToQuotes("[]");
    MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
    assertFalse(it.hasNextValue());
    it.close();
}
