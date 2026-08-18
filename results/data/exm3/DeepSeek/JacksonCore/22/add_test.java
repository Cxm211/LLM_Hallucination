// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesWithoutPathArray() throws Exception
    {
        String jsonString = aposToQuotes("[1,2,3]");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new IndexMatchFilter(0, 2),
                false, // includePath
                false // multipleMatches - false
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals("1", result);
        assertEquals(1, p.getMatchCount());
    }
