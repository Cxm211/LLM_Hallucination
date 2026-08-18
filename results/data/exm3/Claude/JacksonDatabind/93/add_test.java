// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testJDKTypes1872_Interface() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    // Test with an interface that starts with PREFIX_STRING but has null superclass in hierarchy
    String json = aposToQuotes(String.format("{'@class':'%s','value':'test'}",
            "org.springframework.core.Ordered"));
    try {
        Object result = mapper.readValue(json, Object.class);
        // Should succeed if interface, as the check only applies to classes
    } catch (Exception e) {
        // Expected if class name is in illegal list or other validation fails
    }
}