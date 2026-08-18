// com/fasterxml/jackson/databind/creators/ArrayDelegatorCreatorForCollectionTest.java
public void testUnmodifiableWithArrayDelegate() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Class<?> unmodListType = Collections.unmodifiableList(Collections.<String>emptyList()).getClass();
        mapper.addMixIn(unmodListType, UnmodifiableSetMixin.class);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        final String EXPECTED_JSON = "[\""+unmodListType.getName()+"\",[]]";

        List<?> result = mapper.readValue(EXPECTED_JSON, List.class);
        assertTrue(result.isEmpty());
    }