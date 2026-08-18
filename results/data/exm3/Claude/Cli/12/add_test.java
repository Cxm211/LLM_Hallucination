// org/apache/commons/cli/GnuParserTest.java
public void testLongWithEqualDoubleDash() throws Exception
{
    String[] args = new String[] { "--foo=bar" };

    Options options = new Options();
    options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create());

    Parser parser = new GnuParser();
    CommandLine cl = parser.parse(options, args);

    assertEquals("bar", cl.getOptionValue("foo"));
}