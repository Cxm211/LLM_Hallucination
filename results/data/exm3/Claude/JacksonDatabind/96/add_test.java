// com/fasterxml/jackson/databind/deser/creators/CreatorWithNamingStrategyTest.java
public void testSnakeCaseWithOneArgWriteOnly() throws Exception
    {
        final String MSG = "writeOnly";
        OnePropertyWriteOnly actual = MAPPER.readValue(
                "{\"param_name0\":\"" + MSG + "\"}",
                OnePropertyWriteOnly.class);
        assertEquals("CTOR:" + MSG, actual.paramName0);
    }

    static class OnePropertyWriteOnly {
        String paramName0;
        @JsonCreator
        public OnePropertyWriteOnly(@JsonProperty("param_name0") String paramName0) {
            this.paramName0 = "CTOR:" + paramName0;
        }
        @JsonProperty("param_name0")
        public void setParamName0(String value) {
            this.paramName0 = "SETTER:" + value;
        }
    }