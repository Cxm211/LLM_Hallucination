// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java
public void testTooBigBigDecimalAdditionalScales() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        BigDecimal TOO_BIG2 = new BigDecimal("1E+12345");
        BigDecimal TOO_SMALL2 = new BigDecimal("1E-12345");

        for (boolean useBytes : new boolean[] { false, true } ) {
            for (boolean asString : new boolean[] { false, true } ) {
                for (BigDecimal input : new BigDecimal[] { TOO_BIG2, TOO_SMALL2 }) {
                    JsonGenerator g;
                    if (useBytes) {
                        g = f.createGenerator(new ByteArrayOutputStream());
                    } else {
                        g = f.createGenerator(new StringWriter());
                    }
                    if (asString) {
                        g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
                    }
                    try {
                        g.writeNumber(input);
                        fail("Should not have written without exception: "+input);
                    } catch (JsonGenerationException e) {
                        verifyException(e, "Attempt to write plain `java.math.BigDecimal`");
                        verifyException(e, "illegal scale");
                    }
                    g.close();
                }
            }
        }
    }
