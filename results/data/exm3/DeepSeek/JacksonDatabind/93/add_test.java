// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testSpringInterface() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    String json = aposToQuotes(String.format("{'@class':'%s'}",
            org.springframework.context.ApplicationContext.class.getName()));
    Object result = mapper.readValue(json, Object.class);
    assertNotNull(result);
}
