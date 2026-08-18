// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java::testUnfinishedDecimalEntity
public void testUnfinishedDecimalEntity() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "Value: &#48 is zero";
        String expected = "Value: \u0030 is zero";
        assertEquals(expected, neu.translate(input));
    }