// com/fasterxml/jackson/core/json/TestParserNonStandard.java
public void testDecimalWithResultArray() throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(\"123.45\");
        p.nextToken();
        String text = p.getText();
        BigDecimal bd = p.getDecimalValue();
        assertEquals(new BigDecimal(\"123.45\"), bd);
    }
