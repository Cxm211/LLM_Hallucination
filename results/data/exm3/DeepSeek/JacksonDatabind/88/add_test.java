// com/fasterxml/jackson/databind/jsontype/GenericTypeId1735Test.java
public void testNestedTypeCheck1735_ArrayList() throws Exception
    {
        try {
            MAPPER.readValue(aposToQuotes(
"{'w':{'type':'java.util.ArrayList<java.lang.String>'}}"),
                    Wrapper1735.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "not subtype of");
        }
    }
