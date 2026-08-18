// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesFieldAfterObject() throws Exception
{
    String jsonString = aposToQuotes("{'data':{},'value':7}");
    JsonParser p0 = JSON_F.createParser(jsonString);
    JsonParser p = new FilteringParserDelegate(p0,
           new NameMatchFilter("value"),
               false, // includePath
               false // multipleMatches -false
            );
    String result = readAndWrite(JSON_F, p);
    assertEquals(aposToQuotes("7"), result);
}