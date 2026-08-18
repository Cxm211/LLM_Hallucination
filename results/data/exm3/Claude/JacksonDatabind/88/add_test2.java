// com/fasterxml/jackson/databind/jsontype/GenericTypeId1735Test.java
public void testNestedTypeCheckWithoutGenerics() throws Exception
{
    // Test that simple class names without generics still work
    String json = aposToQuotes(
        "{'w':{'type':'java.util.HashMap'}}");
    Wrapper1735 result = MAPPER.readValue(json, Wrapper1735.class);
    assertNotNull(result);
    assertNotNull(result.w);
}