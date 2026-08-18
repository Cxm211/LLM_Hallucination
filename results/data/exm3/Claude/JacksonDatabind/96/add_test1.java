// com/fasterxml/jackson/databind/deser/creators/CreatorWithNamingStrategyTest.java
public void testSnakeCaseWithOneArgNoAccessors() throws Exception
    {
        final String MSG = "noAccessor";
        OnePropertyNoAccessors actual = MAPPER.readValue(
                "{\"param_name0\":\"" + MSG + "\"}",
                OnePropertyNoAccessors.class);
        assertEquals("CTOR:" + MSG, actual.paramName0);
    }

    static class OnePropertyNoAccessors {
        String paramName0;
        @JsonCreator
        public OnePropertyNoAccessors(@JsonProperty("param_name0") String paramName0) {
            this.paramName0 = "CTOR:" + paramName0;
        }
    }