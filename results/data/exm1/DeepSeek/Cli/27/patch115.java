// org.apache.commons.cli.ParserTestCase::testUnambiguousPartialLongOption2
    public void testUnambiguousPartialLongOption2() throws Exception
    {
        String[] args = new String[] { "-ver" };
        
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("version").create());
        options.addOption(OptionBuilder.withLongOpt("help").create());
        
        CommandLine cl = parser.parse(options, args);
        
        assertTrue("Confirm --version is set", cl.hasOption("version"));
    }