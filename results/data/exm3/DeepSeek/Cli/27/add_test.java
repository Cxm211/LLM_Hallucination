// org/apache/commons/cli/ParserTestCase.java
public void testOptionGroupLongReselect() throws Exception
{
    OptionGroup group = new OptionGroup();
    Option foo = OptionBuilder.withLongOpt("foo").create();
    group.addOption(foo);
    group.addOption(OptionBuilder.withLongOpt("bar").create());
    
    // select foo
    group.setSelected(foo);
    assertEquals("foo", group.getSelected());
    
    // reselect foo (should not throw)
    group.setSelected(foo);
    assertEquals("foo", group.getSelected());
}
