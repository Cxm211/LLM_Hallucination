// org/apache/commons/lang3/time/FastDateParserTest.java
@Test
    public void testUnterminatedQuoteAdditional() throws Exception {
        testSdfAndFdp("d'", "d3", true);
        testSdfAndFdp("'d''", "d3", true);
    }
