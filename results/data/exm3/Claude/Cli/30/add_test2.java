// org/apache/commons/cli/OptionGroupTest.java
public void testThreeOptionsFromTwoGroups() throws Exception
{
    OptionGroup group3 = new OptionGroup();
    group3.addOption(new Option("p", null));
    group3.addOption(new Option("q", null));
    _options.addOptionGroup(group3);
    
    String[] args = new String[] { "-f" };
    
    Properties properties = new Properties();
    properties.put("p", "true");
    
    CommandLine cl = parser.parse(_options, args, properties);
    assertTrue(cl.hasOption("f"));
    assertTrue(cl.hasOption("p"));
    assertTrue(!cl.hasOption("q"));
}