// com/fasterxml/jackson/core/json/TestParserNonStandard.java
public void testDecimalWithSegments() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('1');
        }
        String longNumber = sb.toString();
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(longNumber);
        p.nextToken();
        BigDecimal bd = p.getDecimalValue();
        assertEquals(new BigDecimal(longNumber), bd);
    }
