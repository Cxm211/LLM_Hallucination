// org/apache/commons/lang3/time/FastDateParserTest.java
@Test
    public void testLANG_831_quotes() throws Exception {
        testSdfAndFdp("'?'", "?", true);
    }
