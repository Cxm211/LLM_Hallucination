// org.apache.commons.cli.ParserTestCase::testLongWithEqualSingleDash
    public void testLongWithEqualSingleDash() throws Exception
    {
        String[] args = new String[] { "-foo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }