// com/fasterxml/jackson/databind/creators/ArrayDelegatorCreatorForCollectionTest.java
public void testCollectionWithContentConverter() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    TestContentConverterBean result = mapper.readValue("{\"values\":[\"1\",\"2\"]}", TestContentConverterBean.class);
    assertNotNull(result);
    assertEquals(Arrays.asList(1, 2), result.values);
}

static class TestContentConverterBean {
    @JsonDeserialize(contentConverter = TestStringToIntegerConverter.class)
    public List<Integer> values;
}

static class TestStringToIntegerConverter extends StdConverter<String, Integer> {
    @Override
    public Integer convert(String value) {
        return Integer.valueOf(value);
    }
}
