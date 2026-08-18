// org.apache.commons.cli.ParserTestCase::testUnambiguousPartialLongOption1
    public void testUnambiguousPartialLongOption1() throws Exception
    {
        String[] args = new String[] { "--ver" };
        
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("version").create());
        options.addOption(OptionBuilder.withLongOpt("help").create());
        
        CommandLine cl = parser.parse(options, args);
        
        assertTrue("Confirm --version is set", cl.hasOption("version"));
    }