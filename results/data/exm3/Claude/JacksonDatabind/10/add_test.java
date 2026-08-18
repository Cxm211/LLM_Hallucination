// com/fasterxml/jackson/databind/ser/TestAnyGetter.java
public void testIssue705WithNullValue() throws Exception
{
    Issue705Bean input = new Issue705Bean("key", null);
    String json = MAPPER.writeValueAsString(input);
    assertEquals("{\"stuff\":\"[key/null]\"}", json);
}