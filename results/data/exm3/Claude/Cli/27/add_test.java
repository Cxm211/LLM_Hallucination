// org/apache/commons/cli/ParserTestCase.java
public void testOptionGroupShortOnly() throws Exception
{
    OptionGroup group = new OptionGroup();
    group.addOption(OptionBuilder.create('a'));
    group.addOption(OptionBuilder.create('b'));
    
    Options options = new Options();
    options.addOptionGroup(group);
    
    CommandLine cl = parser.parse(options, new String[] { "-b" });
    
    assertTrue(cl.hasOption("b"));
    assertEquals("selected option", "b", group.getSelected());
}