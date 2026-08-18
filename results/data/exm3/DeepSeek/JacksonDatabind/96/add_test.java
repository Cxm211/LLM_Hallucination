// com/fasterxml/jackson/databind/deser/creators/CreatorWithNamingStrategyTest.java
public void testKebabCaseWithOneArg() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
    final String MSG = "test";
    OneProperty actual = mapper.readValue(
            "{\"param-name0\":\""+MSG+"\"}",
            OneProperty.class);
    assertEquals("CTOR:"+MSG, actual.paramName0);
}
