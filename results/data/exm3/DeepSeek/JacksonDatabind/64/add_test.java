// com/fasterxml/jackson/databind/filter/JsonIncludeTest.java
public void testIssue1351Boolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new Issue1351BooleanBean(false)));
    }

    static class Issue1351BooleanBean {
        public boolean value;
        public Issue1351BooleanBean(boolean v) { value = v; }
        public Issue1351BooleanBean() { this(false); }
    }
