// com/fasterxml/jackson/databind/ser/jdk/BigDecimalPlain2230Test.java
public void testBigDecimalWithoutStringShape() throws Exception
    {
        final BigDecimal BD_VALUE = new BigDecimal("123.456");
        final ObjectMapper m = jsonMapperBuilder()
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .build();
        String json = m.writeValueAsString(BD_VALUE);
        assertEquals("123.456", json);
    }