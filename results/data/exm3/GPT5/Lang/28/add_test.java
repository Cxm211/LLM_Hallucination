// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java::testIncompleteEntityNoCrash
public void testIncompleteEntityNoCrash() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("&", neu.translate("&"));
        assertEquals("&#", neu.translate("#", 1));
        assertEquals("&#", neu.translate("&#"));
        assertEquals("&#x", neu.translate("&#x"));
    }