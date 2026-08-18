// org.apache.commons.cli.ParserTestCase::testPartialLongOptionSingleDash
    public void testPartialLongOptionSingleDash() throws Exception
    {
        String[] args = new String[] { "-ver" };
        
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("version").create());
        options.addOption(OptionBuilder.hasArg().create('v'));
        
        CommandLine cl = parser.parse(options, args);
        
        assertTrue("Confirm --version is set", cl.hasOption("version"));
        assertTrue("Confirm -v is not set", !cl.hasOption("v"));
    }