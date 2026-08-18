// com/fasterxml/jackson/databind/ser/jdk/BigDecimalPlain2230Test.java
public void testBigIntegerWithStringShapeFormat() throws Exception
    {
        final BigInteger BI_VALUE = new BigInteger("123456789012345678901234567890");
        final ObjectMapper m = jsonMapperBuilder()
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .build();
        String json = m.writeValueAsString(BI_VALUE);
        assertEquals("123456789012345678901234567890", json);
    }