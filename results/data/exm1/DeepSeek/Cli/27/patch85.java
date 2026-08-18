// org.apache.commons.cli.OptionsTest::testGetMatchingOpts
    public void testGetMatchingOpts()
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("version").create());
        options.addOption(OptionBuilder.withLongOpt("verbose").create());
        
        assertTrue(options.getMatchingOptions("foo").isEmpty());
        assertEquals(1, options.getMatchingOptions("version").size());
        assertEquals(2, options.getMatchingOptions("ver").size());
    }