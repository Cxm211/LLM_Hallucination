// org/apache/commons/cli/OptionBuilderTest.java
public void testTypeNotResetAfterCreate() {
    Option opt1 = OptionBuilder.withType(Integer.class).create("a");
    assertEquals(Integer.class, opt1.getType());
    
    Option opt2 = OptionBuilder.create("b");
    assertEquals(String.class, opt2.getType());
}