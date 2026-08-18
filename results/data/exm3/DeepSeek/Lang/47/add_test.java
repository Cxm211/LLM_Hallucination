// org/apache/commons/lang/text/StrBuilderTest.java
public void testLang412Left_ToStringNull() {
        StrBuilder sb = new StrBuilder();
        Object obj = new Object() {
            @Override
            public String toString() {
                return null;
            }
        };
        sb.appendFixedWidthPadLeft(obj, 5, '#');
        assertEquals("Failed to handle null toString in appendFixedWidthPadLeft", "#####", sb.toString());
    }
