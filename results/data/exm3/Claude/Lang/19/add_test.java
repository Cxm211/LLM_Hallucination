// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testDecimalEntityWithoutSemicolon() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "Test &#65 text";
        String expected = "Test A text";
        String result = neu.translate(input);
        assertEquals("Failed to support decimal entity without semicolon", expected, result);
    }