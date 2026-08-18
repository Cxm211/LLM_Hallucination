// com/fasterxml/jackson/databind/creators/ArrayDelegatorCreatorForCollectionTest.java::testUnmodifiableList
public void testUnmodifiableList() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Class<?> unmodListType = java.util.Collections.unmodifiableList(java.util.Collections.<String>emptyList()).getClass();
        mapper.addMixIn(unmodListType, UnmodifiableListMixin.class);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        final String EXPECTED_JSON = "[\""+unmodListType.getName()+"\",[]]";

        java.util.List<?> foo = mapper.readValue(EXPECTED_JSON, java.util.List.class);
        assertTrue(foo.isEmpty());
    }