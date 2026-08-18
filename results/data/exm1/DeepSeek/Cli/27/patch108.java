// org.apache.commons.cli.ParserTestCase::testShortWithEqual
    public void testShortWithEqual() throws Exception
    {
        String[] args = new String[] { "-f=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }