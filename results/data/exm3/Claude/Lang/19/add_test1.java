// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testValidEntitiesWithSemicolon() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#65;&#x42;";
        String expected = "AB";
        String result = neu.translate(input);
        assertEquals("Failed to translate valid entities with semicolons", expected, result);
    }