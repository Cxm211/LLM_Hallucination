// org/apache/commons/lang/text/ExtendedMessageFormatTest.java
public void testMultipleEscapedQuotes_LANG_477() {
    String pattern = "it''s a {0,lower}''test''!";
    ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
    assertEquals("it's a dummy'test'!", emf.format(new Object[] {"DUMMY"}));
}