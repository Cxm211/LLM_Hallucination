// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testIncompleteEntityMissingSemicolon() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#123";
        String expected = "&#123";
        String result = neu.translate(input);
        assertEquals("Incomplete entity without semicolon should not be translated", expected, result);
    }
