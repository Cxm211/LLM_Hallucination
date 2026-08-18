// org/apache/commons/lang/text/StrBuilderTest.java::testLang412Left
        StrBuilder sb2 = new StrBuilder();
        Object objWithNullToString = new Object() { public String toString() { return null; } };
        sb2.appendFixedWidthPadLeft(objWithNullToString, 5, '#');
        assertEquals("Handle object with null toString in appendFixedWidthPadLeft", "#####", sb2.toString());
