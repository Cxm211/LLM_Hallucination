// org/mockitousage/bugs/deepstubs/DeepStubFailingWhenGenricNestedAsRawTypeTest.java::discoverDeepMockingOfGenerics
@Test
public void deepStubsWorkOnRawGeneric() {
  // Helper class to simulate raw generic usage
  class Box<T> {
    public Box<T> next() { return null; }
    public T val() { return null; }
  }
  Box mockBox = mock(Box.class, RETURNS_DEEP_STUBS);
  when(mockBox.next().next().val()).thenReturn("X");
}