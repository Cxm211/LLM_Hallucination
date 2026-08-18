// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java
public void testBigDecimalWithoutPlainFeature() throws Exception
{
    JsonFactory f = new JsonFactory();

    BigDecimal TOO_BIG = new BigDecimal("1E+10000");
    BigDecimal TOO_SMALL = new BigDecimal("1E-10000");

    for (boolean useBytes : new boolean[] { false, true } ) {
        for (BigDecimal input : new BigDecimal[] { TOO_BIG, TOO_SMALL }) {
            JsonGenerator g;
            
            if (useBytes) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                g = f.createGenerator(out);
                g.writeNumber(input);
                g.close();
                String result = out.toString("UTF-8");
                assertNotNull(result);
            } else {
                StringWriter sw = new StringWriter();
                g = f.createGenerator(sw);
                g.writeNumber(input);
                g.close();
                String result = sw.toString();
                assertNotNull(result);
            }
        }
    }
}