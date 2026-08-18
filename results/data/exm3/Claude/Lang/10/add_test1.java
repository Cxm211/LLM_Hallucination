// org/apache/commons/lang3/time/FastDateParserTest.java
@Test
public void testEscapeRegexSpecialChars() throws Exception {
    testSdfAndFdp("yyyy-MM-dd","2023-01-15", true);
}