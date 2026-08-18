// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNestedObjectFilteringWithPathAndMultipleMatches() throws Exception
{
    String jsonString = aposToQuotes("{\"outer\":{\"inner\":{\"value\":10},\"other\":5},\"value\":20}");
    JsonParser p0 = JSON_F.createParser(jsonString);
    FilteringParserDelegate p = new FilteringParserDelegate(p0,
            new NameMatchFilter(\"value\"),
            true,
            true
    );
    String result = readAndWrite(JSON_F, p);
    assertEquals(aposToQuotes("{\"outer\":{\"inner\":{\"value\":10}},\"value\":20}"), result);
    assertEquals(2, p.getMatchCount());
}