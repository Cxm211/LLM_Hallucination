// org/mockito/internal/matchers/EqualityTest.java
@Test
public void shouldHandleNullComparisons() {
    assertTrue(areEqual(null, null));
    assertFalse(areEqual(null, new Object()));
    assertFalse(areEqual(new Object(), null));
    assertFalse(areEqual(null, new int[] {1}));
    assertFalse(areEqual(new int[] {1}, null));
}