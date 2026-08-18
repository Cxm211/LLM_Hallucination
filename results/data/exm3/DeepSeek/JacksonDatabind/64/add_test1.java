// com/fasterxml/jackson/databind/filter/JsonIncludeTest.java
public void testIssue1351Array() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new Issue1351ArrayBean(new int[0])));
    }

    static class Issue1351ArrayBean {
        public int[] values;
        public Issue1351ArrayBean(int[] v) { values = v; }
        public Issue1351ArrayBean() { this(new int[0]); }
    }
