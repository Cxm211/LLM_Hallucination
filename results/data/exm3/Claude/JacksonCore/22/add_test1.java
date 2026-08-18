// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNestedArrayFilteringWithPathAndMultipleMatches() throws Exception
{
    String jsonString = aposToQuotes("{\"a\":[[1,2],[3,4]],\"b\":[[5,6],[7,8]]}");
    JsonParser p0 = JSON_F.createParser(jsonString);
    FilteringParserDelegate p = new FilteringParserDelegate(p0,
            new IndexMatchFilter(0),
            true,
            true
    );
    String result = readAndWrite(JSON_F, p);
    assertEquals(aposToQuotes("{\"a\":[[1],[3]],\"b\":[[5],[7]]}"), result);
    assertEquals(4, p.getMatchCount());
}