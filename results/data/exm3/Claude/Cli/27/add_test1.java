// org/apache/commons/cli/ParserTestCase.java
public void testOptionGroupMixed() throws Exception
{
    OptionGroup group = new OptionGroup();
    group.addOption(OptionBuilder.hasArg().create('x'));
    group.addOption(OptionBuilder.withLongOpt("longopt").create());
    
    Options options = new Options();
    options.addOptionGroup(group);
    
    CommandLine cl = parser.parse(options, new String[] { "--longopt" });
    
    assertTrue(cl.hasOption("longopt"));
    assertEquals("selected option", "longopt", group.getSelected());
}