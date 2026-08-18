// com/fasterxml/jackson/databind/struct/TestPOJOAsArray.java
public void testDefaultValueSuppression() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT, JsonInclude.Include.ALWAYS));
    String json = mapper.writeValueAsString(new TwoStringsBean("default", "bar"));
    assertTrue("Should serialize with default value handling", json.contains("bar"));
}