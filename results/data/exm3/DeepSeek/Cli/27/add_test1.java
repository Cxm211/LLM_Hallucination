// org/apache/commons/cli/ParserTestCase.java
public void testOptionGroupLongException() throws Exception
{
    OptionGroup group = new OptionGroup();
    Option foo = OptionBuilder.withLongOpt("foo").create();
    Option bar = OptionBuilder.withLongOpt("bar").create();
    group.addOption(foo);
    group.addOption(bar);
    
    // select foo
    group.setSelected(foo);
    
    // try to select bar, should throw AlreadySelectedException
    try {
        group.setSelected(bar);
        fail("Expected AlreadySelectedException");
    } catch (AlreadySelectedException e) {
        // expected
    }
}
