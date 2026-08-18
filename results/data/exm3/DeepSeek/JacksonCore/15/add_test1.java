// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesWithIncludePath() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'ob':{'value':3},'value':4}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   true, // includePath
                   false // multipleMatches -false
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3}}"), result);
    }
