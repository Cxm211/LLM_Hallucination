// com/fasterxml/jackson/databind/ser/TestAnyGetter.java
public void testIssue705WithEmptyKey() throws Exception
{
    Issue705Bean input = new Issue705Bean("", "value");
    String json = MAPPER.writeValueAsString(input);
    assertEquals("{\"stuff\":\"[/value]\"}", json);
}