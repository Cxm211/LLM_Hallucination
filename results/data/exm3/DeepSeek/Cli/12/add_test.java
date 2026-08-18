// org/apache/commons/cli/GnuParserTest.java
public void testLongWithEqualInValue() throws Exception
    {
        String[] args = new String[] { "--foo=bar=baz" };
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));
        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);
        assertEquals("bar=baz", cl.getOptionValue("foo"));
    }
