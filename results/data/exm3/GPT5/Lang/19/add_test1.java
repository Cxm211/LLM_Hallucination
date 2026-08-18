// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java::testSupplementaryEntity
public void testSupplementaryEntity() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // Musical symbol G clef U+1D11E
        String input = "Clef: &#x1D11E;";
        String expected = "Clef: \uD834\uDD1E";
        assertEquals(expected, neu.translate(input));
    }