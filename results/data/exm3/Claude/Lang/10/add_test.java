// org/apache/commons/lang3/time/FastDateParserTest.java
@Test
public void testEscapeRegexMultipleSpaces() throws Exception {
    testSdfAndFdp("MMM    dd","Jan    15", true);
}