// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java
public void testBigDecimalWithinBounds() throws Exception
{
    JsonFactory f = new JsonFactory();
    f.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

    BigDecimal EDGE_UPPER = new BigDecimal("1E+9999");
    BigDecimal EDGE_LOWER = new BigDecimal("1E-9999");
    BigDecimal ZERO = new BigDecimal("0");

    for (boolean useBytes : new boolean[] { false, true } ) {
        JsonGenerator g;
        
        if (useBytes) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            g = f.createGenerator(out);
            g.writeStartArray();
            g.writeNumber(EDGE_UPPER);
            g.writeNumber(EDGE_LOWER);
            g.writeNumber(ZERO);
            g.writeEndArray();
            g.close();
            String result = out.toString("UTF-8");
            assertNotNull(result);
        } else {
            StringWriter sw = new StringWriter();
            g = f.createGenerator(sw);
            g.writeStartArray();
            g.writeNumber(EDGE_UPPER);
            g.writeNumber(EDGE_LOWER);
            g.writeNumber(ZERO);
            g.writeEndArray();
            g.close();
            String result = sw.toString();
            assertNotNull(result);
        }
    }
}