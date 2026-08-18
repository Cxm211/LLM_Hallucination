// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testIncompleteEntityWithHexPrefix() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#x";
        String expected = "&#x";
        String result = neu.translate(input);
        assertEquals("Incomplete entity \"&#x\" should not be translated", expected, result);
    }
