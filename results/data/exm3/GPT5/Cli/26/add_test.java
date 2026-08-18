// org/apache/commons/cli/OptionBuilderTest.java::testBuilderIsResettedAlways
public void testBuilderIsResettedOnNullCreate() {
        try {
            OptionBuilder.withDescription("JUnit").create((String) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());
    }