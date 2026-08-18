// org/apache/commons/lang3/time/FastDateParserTest.java::testLANG_832
@Test
public void testLANG_832_additional_unterminatedQuote() throws Exception {
    testSdfAndFdp("'abc", "abc", true); // should fail (unterminated quote)
}