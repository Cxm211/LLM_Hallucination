// org/apache/commons/cli/OptionBuilderTest.java
public void testBuilderIsResettedOnSuccess() {
    OptionBuilder.withDescription("Test Description");
    OptionBuilder.hasArg();
    OptionBuilder.isRequired();
    Option opt = OptionBuilder.create('a');
    assertNotNull("Option should be created", opt);
    assertEquals("Description should match", "Test Description", opt.getDescription());
    assertTrue("Option should have arg", opt.hasArg());
    assertTrue("Option should be required", opt.isRequired());
    
    // Create another option without setting properties
    Option opt2 = OptionBuilder.create('b');
    assertNull("Description should be null after reset", opt2.getDescription());
    assertFalse("Option should not have arg after reset", opt2.hasArg());
    assertFalse("Option should not be required after reset", opt2.isRequired());
}