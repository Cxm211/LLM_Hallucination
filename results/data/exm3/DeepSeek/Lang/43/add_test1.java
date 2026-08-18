// org/apache/commons/lang/text/ExtendedMessageFormatTest.java
public void testEmptyQuotedString() {
        String pattern = "''";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("", emf.format(new Object[] {}));
    }
