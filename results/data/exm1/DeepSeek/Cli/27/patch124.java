// org.apache.commons.cli.ParserTestCase::testOptionAndRequiredOption
    public void testOptionAndRequiredOption() throws Exception
    {
        String[] args = new String[] { "-a", "-b", "file" };
        
        Options options = new Options();
        options.addOption("a", "enable-a", false, null);
        options.addOption(OptionBuilder.withLongOpt("bfile").hasArg().isRequired().create('b'));

        CommandLine cl = parser.parse(options,args);

        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("file"));
        assertTrue("Confirm NO of extra args", cl.getArgList().size() == 0);
    }