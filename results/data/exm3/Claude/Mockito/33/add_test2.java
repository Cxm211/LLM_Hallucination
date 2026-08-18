// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java
@Test
public void shouldVerificationFailWhenNotCalled() {
    try {
        verify(iterable).iterator();
        Assert.fail("Expected verification to fail");
    } catch (AssertionError e) {
        // Expected
    }
}