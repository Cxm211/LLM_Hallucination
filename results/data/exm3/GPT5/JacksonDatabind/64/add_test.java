// com/fasterxml/jackson/databind/filter/JsonIncludeTest.java
public void testIssue1351b() throws Exception {
        class NoDefCtorBool {
            public boolean b;
            public NoDefCtorBool(boolean b) { this.b = b; }
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        assertEquals(aposToQuotes("{}"), mapper.writeValueAsString(new NoDefCtorBool(false)));
    }