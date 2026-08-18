// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testSupplementaryHexUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#x10C22;";
        String expected = "\uD803\uDC22";
        String result = neu.translate(input);
        assertEquals("Failed to unescape supplementary hex entity", expected, result);
    }
