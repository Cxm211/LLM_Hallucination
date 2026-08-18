// com/fasterxml/jackson/databind/filter/JsonIncludeTest.java
public void testIssue1351AdditionalCoverage() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        // Test with non-zero value to ensure it's included
        assertEquals(aposToQuotes("{'value':1.5}"),
                mapper.writeValueAsString(new Issue1351Bean(null, 1.5)));
        // Test with non-default int to ensure it's included
        assertEquals(aposToQuotes("{'value':5}"),
                mapper.writeValueAsString(new Issue1351NonBean(5)));
    }