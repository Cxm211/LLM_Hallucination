// org/apache/commons/cli2/bug/BugCLI123Test.java
public void testNestedGroupWithMultipleOptions() throws OptionException {
    final DefaultOption parentOpt = new DefaultOption();
    parentOpt.setShortPrefix("-");
    parentOpt.setLongPrefix("--");
    parentOpt.setPreferredName("parent");
    parentOpt.setRequired(false);
    
    final DefaultOption child1 = new DefaultOption();
    child1.setShortPrefix("-");
    child1.setLongPrefix("--");
    child1.setPreferredName("child1");
    child1.setRequired(false);
    
    final DefaultOption child2 = new DefaultOption();
    child2.setShortPrefix("-");
    child2.setLongPrefix("--");
    child2.setPreferredName("child2");
    child2.setRequired(false);
    
    final Group nestedGroup = new GroupBuilder()
        .withName("nested")
        .withOption(child1)
        .withOption(child2)
        .withMinimum(0)
        .withMaximum(2)
        .create();
    
    final Group parentGroup = new GroupBuilder()
        .withName("parent")
        .withOption(parentOpt)
        .withOption(nestedGroup)
        .withMinimum(0)
        .withMaximum(3)
        .create();
    
    final Parser parser = new Parser();
    parser.setGroup(parentGroup);
    
    CommandLine cl = parser.parse(new String[] { "--parent", "--child1", "--child2" });
    assertTrue("Parent option not found", cl.hasOption(parentOpt));
    assertTrue("Child1 option not found", cl.hasOption(child1));
    assertTrue("Child2 option not found", cl.hasOption(child2));
}