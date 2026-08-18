// org/apache/commons/lang3/time/FastDateParserTest.java
@Test
public void testEscapeRegexTrailingWhitespace() throws Exception {
    testSdfAndFdp("M ","3 ", true);
}