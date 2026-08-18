// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeIdWithEnum1328Test.java
public void testWithCatEnum() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(Arrays.asList(new AnimalAndType(AnimalType.Cat, new Cat())));
    List<AnimalAndType> list = mapper.readerFor(new TypeReference<List<AnimalAndType>>() { })
        .readValue(json);
    assertNotNull(list);
    assertEquals(AnimalType.Cat, list.get(0).getType());
}
