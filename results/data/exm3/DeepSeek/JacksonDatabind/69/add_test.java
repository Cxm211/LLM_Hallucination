// com/fasterxml/jackson/databind/creators/Creator1476Test.java
public void testExplicitImplicitPropertyCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // SimplePojo with two constructors: one explicit @JsonCreator, one implicit
        // This test ensures explicit creator is chosen.
        // Define a simple class for testing
        static class ExplicitImplicitPojo {
            private final int value;
            private final String text;
            @JsonCreator
            public ExplicitImplicitPojo(@JsonProperty("val") int v, @JsonProperty("txt") String t) {
                this.value = v;
                this.text = t;
            }
            // implicit constructor (no annotation) with different parameter order
            public ExplicitImplicitPojo(String t, int v) {
                this.value = v;
                this.text = t;
            }
            public int getValue() { return value; }
            public String getText() { return text; }
        }
        ExplicitImplicitPojo pojo = mapper.readValue("{ \"val\": 42, \"txt\": \"hello\" }", ExplicitImplicitPojo.class);
        assertEquals(42, pojo.getValue());
        assertEquals("hello", pojo.getText());
    }
