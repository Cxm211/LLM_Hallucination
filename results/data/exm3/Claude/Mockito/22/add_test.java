// org/mockito/internal/matchers/EqualityTest.java
@Test
public void shouldReturnFalseWhenComparingNonArrayWithArray() {
    assertFalse(areEqual("string", new String[] {"string"}));
    assertFalse(areEqual(Integer.valueOf(1), new int[] {1}));
    assertFalse(areEqual(new Object(), new Object[1]));
}