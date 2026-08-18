// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testSupplementaryCharacter() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "Test &#x1F600;";
        String expected = "Test \uD83D\uDE00";
        String result = neu.translate(input);
        assertEquals("Failed to handle supplementary character", expected, result);
    }