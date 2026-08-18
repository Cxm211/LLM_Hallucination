// com/fasterxml/jackson/databind/ser/jdk/BigDecimalPlain2230Test.java
public void testBigDecimalAsPlainLarge() throws Exception
    {
        final String NORM_VALUE = "10000000000";
        final BigDecimal BD_VALUE = new BigDecimal("1e10");
        final BigDecimalAsString INPUT = new BigDecimalAsString(BD_VALUE);
        // by default, use the default `toString()` which may be scientific
        assertEquals("{\"value\":\""+BD_VALUE.toString()+"\"}", MAPPER.writeValueAsString(INPUT));

        // but can force to "plain" notation
        final ObjectMapper m = jsonMapperBuilder()
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .build();
        assertEquals("{\"value\":\""+NORM_VALUE+"\"}", m.writeValueAsString(INPUT));
    }
