// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testBigDecimalScientificNotation() throws Exception {
        BigDecimal bd = new BigDecimal("1.23e-500");
        JsonNode node = MAPPER.valueToTree(bd);
        assertTrue(node.isBigDecimal());
        assertEquals(bd, node.decimalValue());
    }
