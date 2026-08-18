// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testInvalidCodePoint() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#1114112;";
        String expected = "&#1114112;";
        String result = neu.translate(input);
        assertEquals("Invalid code point should not be translated", expected, result);
    }
