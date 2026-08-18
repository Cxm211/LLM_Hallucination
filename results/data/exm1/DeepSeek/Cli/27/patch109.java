// org.apache.commons.cli.ParserTestCase::testShortWithoutEqual
    public void testShortWithoutEqual() throws Exception
    {
        String[] args = new String[] { "-fbar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }