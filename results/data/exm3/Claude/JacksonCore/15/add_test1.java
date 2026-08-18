// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesNestedObject() throws Exception
{
    String jsonString = aposToQuotes("{'outer':{'value':10},'value':20}");
    JsonParser p0 = JSON_F.createParser(jsonString);
    JsonParser p = new FilteringParserDelegate(p0,
           new NameMatchFilter("value"),
               false, // includePath
               false // multipleMatches -false
            );
    String result = readAndWrite(JSON_F, p);
    assertEquals(aposToQuotes("10"), result);
}