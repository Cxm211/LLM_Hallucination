// org/apache/commons/cli2/CommandLineTestCase.java
public final void testGetOptions_WithEmptyGroup() throws OptionException {
    final Option help = DefaultOptionTest.buildHelpOption();
    final Group emptyGroup = new GroupBuilder().withName("empty").withMinimum(0).withMaximum(0).create();
    
    final Group mainGroup = new GroupBuilder()
        .withOption(help)
        .withOption(emptyGroup)
        .create();
    
    final Parser parser = new Parser();
    parser.setGroup(mainGroup);
    
    final CommandLine cl = parser.parse(new String[] { "--help" });
    
    final Iterator i = cl.getOptions().iterator();
    assertSame(mainGroup, i.next());
    assertSame(help, i.next());
    assertFalse(i.hasNext());
}