// org.apache.commons.cli.BugsTest::test15046
    public void test15046() throws Exception
    {
        CommandLineParser parser = new PosixParser();
        String[] CLI_ARGS = new String[] {"-z", "c"};

        Options options = new Options();
        options.addOption(new Option("z", "timezone", true, "affected option"));

        parser.parse(options, CLI_ARGS);
        
        
        options.addOption("c", "conflict", true, "conflict option");
        CommandLine line = parser.parse(options, CLI_ARGS);
        assertEquals( line.getOptionValue('z'), "c" );
        assertTrue( !line.hasOption("c") );
    }