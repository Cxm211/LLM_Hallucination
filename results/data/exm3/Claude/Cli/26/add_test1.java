// org/apache/commons/cli/OptionBuilderTest.java
public void testBuilderIsResettedOnNullPointerException() {
    OptionBuilder.withDescription("Test Description");
    try {
        OptionBuilder.create((String)null);
        fail("NullPointerException or IllegalArgumentException expected");
    } catch (NullPointerException e) {
        // expected
    } catch (IllegalArgumentException e) {
        // also acceptable
    }
    assertNull("Description should be null after exception", OptionBuilder.create('z').getDescription());
}