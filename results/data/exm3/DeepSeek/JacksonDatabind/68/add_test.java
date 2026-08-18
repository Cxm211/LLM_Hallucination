// com/fasterxml/jackson/databind/struct/SingleValueAsArrayTest.java
public void testDelegateDeserializerWithArray() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Bean1421B<List<String>> result = mapper.readValue("[\"a\", \"b\"]", new TypeReference<Bean1421B<List<String>>>() {});
    List<String> expected = Arrays.asList("a", "b");
    assertEquals(expected, result.value);
}
