// com/fasterxml/jackson/databind/jsontype/TestExternalId.java::testBigDecimal965
public void testDoubleAsBigDecimalPrecision() throws Exception {
        Wrapper965 w = new Wrapper965();
        w.typeEnum = Type965.DOUBLE;
        final String NUM_STR = "0.1";
        w.value = Double.valueOf(NUM_STR);

        String json = MAPPER.writeValueAsString(w);
        if (!json.contains(NUM_STR)) {
            fail("JSON content should contain value '"+NUM_STR+"', does not appear to: "+json);
        }

        Wrapper965 w2 = MAPPER.readerFor(Wrapper965.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readValue(json);

        assertEquals(w.typeEnum, w2.typeEnum);
        assertEquals(new BigDecimal(NUM_STR), (BigDecimal) w2.value);
    }