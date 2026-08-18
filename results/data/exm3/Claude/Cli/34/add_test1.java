// org/apache/commons/cli/OptionBuilderTest.java
public void testMultipleOptionsWithDifferentTypes() {
    Option opt1 = OptionBuilder.withType(Double.class).hasArg().create("x");
    assertEquals(Double.class, opt1.getType());
    
    Option opt2 = OptionBuilder.hasArg().create("y");
    assertEquals(String.class, opt2.getType());
    
    Option opt3 = OptionBuilder.withType(Boolean.class).create("z");
    assertEquals(Boolean.class, opt3.getType());
}