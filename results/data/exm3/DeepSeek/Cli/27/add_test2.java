// org/apache/commons/cli/ParserTestCase.java
public void testOptionGroupReset() throws Exception
{
    OptionGroup group = new OptionGroup();
    Option foo = OptionBuilder.withLongOpt("foo").create();
    Option bar = OptionBuilder.withLongOpt("bar").create();
    group.addOption(foo);
    group.addOption(bar);
    
    // select foo
    group.setSelected(foo);
    assertEquals("foo", group.getSelected());
    
    // reset selection
    group.setSelected(null);
    assertNull(group.getSelected());
    
    // now select bar
    group.setSelected(bar);
    assertEquals("bar", group.getSelected());
}
