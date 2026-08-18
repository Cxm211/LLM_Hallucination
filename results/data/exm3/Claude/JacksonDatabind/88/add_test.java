// com/fasterxml/jackson/databind/jsontype/GenericTypeId1735Test.java
public void testNestedTypeCheckWithSubtype() throws Exception
{
    // Test that a valid subtype with generics passes correctly
    String json = aposToQuotes(
        "{'w':{'type':'java.util.LinkedHashMap<java.lang.String,java.lang.Object>'}}");
    Wrapper1735 result = MAPPER.readValue(json, Wrapper1735.class);
    assertNotNull(result);
    assertNotNull(result.w);
}