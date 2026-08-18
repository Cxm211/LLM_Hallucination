// org/apache/commons/lang/text/ExtendedMessageFormatTest.java
public void testQuotedStringAfterEscapedQuote_LANG_477() {
    String pattern = "it''s a {0,lower} 'quoted' text";
    ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
    assertEquals("it's a dummy quoted text", emf.format(new Object[] {"DUMMY"}));
}