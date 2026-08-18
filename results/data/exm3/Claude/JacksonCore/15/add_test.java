// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesWithArray() throws Exception
{
    String jsonString = aposToQuotes("{'values':[1,2,3],'value':5}");
    JsonParser p0 = JSON_F.createParser(jsonString);
    JsonParser p = new FilteringParserDelegate(p0,
           new NameMatchFilter("value"),
               false, // includePath
               false // multipleMatches -false
            );
    String result = readAndWrite(JSON_F, p);
    assertEquals(aposToQuotes("5"), result);
}