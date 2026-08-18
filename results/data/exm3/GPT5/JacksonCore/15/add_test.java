// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java::testNotAllowMultipleMatchesArray
public void testNotAllowMultipleMatchesArray() throws Exception
    {
        String jsonString = aposToQuotes("{'arr':[1,2],'obj':{'x':1},'arr':[3,4],'y':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("arr"),
                   false, // includePath
                   false // multipleMatches - false
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("[1,2]"), result);
    }