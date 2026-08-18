// org/apache/commons/cli/GnuParserTest.java
public void testPropertiesOptionWithEqual() throws Exception
{
    String[] args = new String[] { "-Dkey=value" };

    Options options = new Options();
    options.addOption(OptionBuilder.withLongOpt("define").hasArgs(2).withValueSeparator().create('D'));

    Parser parser = new GnuParser();
    CommandLine cl = parser.parse(options, args);

    assertEquals("value", cl.getOptionValue('D', "key"));
}