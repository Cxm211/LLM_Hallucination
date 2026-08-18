// org/apache/commons/cli/OptionBuilderTest.java
public void testBuilderIsResettedAfterEmptyOpt() {
    try {
        OptionBuilder.withDescription("JUnit").create("");
        fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
        // expected
    }
    assertNull("Builder should be reset after empty opt", OptionBuilder.create("x").getDescription());
}
