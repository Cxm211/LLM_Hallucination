// org/apache/commons/cli/OptionBuilderTest.java
public void testBuilderIsResettedWithLongOpt() {
    OptionBuilder.withDescription("Long Option Test");
    OptionBuilder.withLongOpt("long-option");
    try {
        OptionBuilder.create('"');
        fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
        // expected
    }
    Option opt = OptionBuilder.create('y');
    assertNull("Description should be null after reset", opt.getDescription());
    assertNull("Long option should be null after reset", opt.getLongOpt());
}