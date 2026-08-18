// org/apache/commons/lang/text/ExtendedMessageFormatTest.java
public void testEscapedQuoteAtEnd_LANG_477() {
    String pattern = "it''s a {0,lower}''";
    ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
    assertEquals("it's a dummy'", emf.format(new Object[] {"DUMMY"}));
}