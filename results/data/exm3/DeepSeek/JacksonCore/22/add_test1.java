// com/fasterxml/jackson/core/filter/BasicParserFilteringTest.java
public void testNotAllowMultipleMatchesWithoutPathObject() throws Exception
    {
        String jsonString = aposToQuotes("{'a':1,'b':2,'c':3}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("a", "c"),
                false, // includePath
                false // multipleMatches - false
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals("1", result);
        assertEquals(1, p.getMatchCount());
    }
