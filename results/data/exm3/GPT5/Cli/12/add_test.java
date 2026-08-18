// org/apache/commons/cli/GnuParserTest.java::testPropertyWithEqualShort
public void testPropertyWithEqualShort() throws Exception
    {
        String[] args = new String[] { "-Dfoo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().create('D'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("foo=bar", cl.getOptionValue("D"));
    }