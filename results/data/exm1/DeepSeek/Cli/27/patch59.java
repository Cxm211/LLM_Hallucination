// org.apache.commons.cli.HelpFormatterTest::testRtrim
    public void testRtrim()
    {
        HelpFormatter formatter = new HelpFormatter();

        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  foo", formatter.rtrim("  foo  "));
    }