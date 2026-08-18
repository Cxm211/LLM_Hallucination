// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testJDKTypes1872_SpringClassHierarchy() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    // Test case where a class in the Spring hierarchy has a null superclass before reaching Object.class
    // This tests the fix for potential NPE when cls.getSuperclass() returns null
    String json = aposToQuotes("{'@class':'org.springframework.aop.framework.AdvisedSupport','value':'test'}");
    try {
        Object result = mapper.readValue(json, Object.class);
        fail("Should not allow Spring framework types");
    } catch (JsonMappingException e) {
        // Expected - should be blocked
        assertTrue(e.getMessage().contains("security reasons"));
    }
}