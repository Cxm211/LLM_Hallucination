// org.apache.commons.cli.ParserTestCase::testUnambiguousPartialLongOption3
    public void testUnambiguousPartialLongOption3() throws Exception
    {
        String[] args = new String[] { "--ver=1" };
        
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("verbose").hasOptionalArg().create());
        options.addOption(OptionBuilder.withLongOpt("help").create());
        
        CommandLine cl = parser.parse(options, args);
        
        assertTrue("Confirm --verbose is set", cl.hasOption("verbose"));
        assertEquals("1", cl.getOptionValue("verbose"));
    }