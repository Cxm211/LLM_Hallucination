// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
public void shouldHandleNullInSameMatcherWithVerificationFailure() {
    mock.objectArgMethod("not null");
    try {
        verify(mock).objectArgMethod(same(null));
        fail("Expected verification to fail");
    } catch (AssertionError e) {
        // Expected - verification should fail and error message should contain "same(null)"
        assertTrue(e.getMessage().contains("same(null)"));
    }
}