// com/fasterxml/jackson/databind/ser/jdk/BigDecimalPlain2230Test.java::testBigIntegerAsPlainTest_AltExponent
public void testBigIntegerAsPlainTest_AltExponent() throws Exception
    {
        final BigDecimal BD_VALUE = new BigDecimal("1E+3");
        final BigDecimalAsString INPUT = new BigDecimalAsString(BD_VALUE);
        // default uses toString()
        assertEquals("{\"value\":\""+BD_VALUE.toString()+"\"}", MAPPER.writeValueAsString(INPUT));

        // with WRITE_BIGDECIMAL_AS_PLAIN enabled, expect plain representation
        final ObjectMapper m = jsonMapperBuilder()
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .build();
        assertEquals("{\"value\":\"1000\"}", m.writeValueAsString(INPUT));
    }