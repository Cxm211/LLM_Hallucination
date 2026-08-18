// com/fasterxml/jackson/databind/jsontype/GenericTypeId1735Test.java
public void testNestedTypeCheckWithComplexGenerics() throws Exception
{
    try {
        MAPPER.readValue(aposToQuotes(
            "{'w':{'type':'java.util.ArrayList<java.lang.Integer>'}}"),
            Wrapper1735.class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        verifyException(e, "not subtype of");
    }
}