// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testBigDecimalWithNaN() throws Exception
    {
        Wrapper965 w = new Wrapper965();
        w.typeEnum = Type965.BIG_DECIMAL;
        w.value = new BigDecimal("12345.6789");

        String json = MAPPER.writeValueAsString(w);

        Wrapper965 w2 = MAPPER.readerFor(Wrapper965.class)
                .without(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readValue(json);

        assertEquals(w.typeEnum, w2.typeEnum);
        assertTrue(w.value.equals(w2.value));
    }