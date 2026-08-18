// org/apache/commons/lang/text/ExtendedMessageFormatTest.java
public void testEscapedQuoteInQuotedString() {
        String pattern = "'it''s a test'";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("it's a test", emf.format(new Object[] {}));
    }
