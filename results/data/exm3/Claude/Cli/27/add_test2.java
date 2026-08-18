// org/apache/commons/cli/ParserTestCase.java
public void testOptionGroupReselect() throws Exception
{
    OptionGroup group = new OptionGroup();
    Option opt = OptionBuilder.withLongOpt("test").create();
    group.addOption(opt);
    
    Options options = new Options();
    options.addOptionGroup(group);
    
    CommandLine cl = parser.parse(options, new String[] { "--test" });
    
    assertTrue(cl.hasOption("test"));
    assertEquals("selected option", "test", group.getSelected());
    
    // Reselect the same option should not throw exception
    group.setSelected(opt);
    assertEquals("selected option", "test", group.getSelected());
}