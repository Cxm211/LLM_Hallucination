// com/fasterxml/jackson/databind/deser/jdk/UtilCollectionsTypesTest.java
public void testUnmodifiableSetFromLinkedHashSet() throws Exception {
    final Set<String> input = new LinkedHashSet<>();
    input.add("first");
    input.add("second");

    Collection<?> act = _writeReadCollection(Collections.unmodifiableSet(input));
    assertEquals(input, act);
}