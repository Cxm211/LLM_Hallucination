// org/apache/commons/lang/text/StrBuilderTest.java
public void testLang412Right_ToStringNull() {
        StrBuilder sb = new StrBuilder();
        Object obj = new Object() {
            @Override
            public String toString() {
                return null;
            }
        };
        sb.appendFixedWidthPadRight(obj, 5, '#');
        assertEquals("Failed to handle null toString in appendFixedWidthPadRight", "#####", sb.toString());
    }
