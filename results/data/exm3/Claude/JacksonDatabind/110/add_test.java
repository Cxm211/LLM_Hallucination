// com/fasterxml/jackson/databind/deser/jdk/UtilCollectionsTypesTest.java
public void testUnmodifiableListFromArrayList() throws Exception {
    final List<String> input = new ArrayList<>();
    input.add("first");
    input.add("second");

    Collection<?> act = _writeReadCollection(Collections.unmodifiableList(input));
    assertEquals(input, act);

    assertEquals(Collections.unmodifiableList(new ArrayList<>(input)).getClass(), act.getClass());
}