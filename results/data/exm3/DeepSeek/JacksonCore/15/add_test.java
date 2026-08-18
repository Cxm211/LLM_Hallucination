// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesArray() throws Exception
    {
        String jsonString = aposToQuotes("[{'value':1},{'value':2}]");
        JsonParser p0 = JSON_F.createParser(jsonString);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, // includePath
                   false // multipleMatches -false
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("1"), result);
    }
