// org/apache/commons/lang/text/ExtendedMessageFormatTest.java::testEscapedQuoteAlone
public void testEscapedQuoteAlone() {
        String pattern = "''";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("'", emf.format(new Object[] {}));
    }