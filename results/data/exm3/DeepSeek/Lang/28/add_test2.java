// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testIncompleteEntityOnlyAmpersandHash() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#";
        String expected = "&#";
        String result = neu.translate(input);
        assertEquals("Incomplete entity \"&#\" should not be translated", expected, result);
    }
