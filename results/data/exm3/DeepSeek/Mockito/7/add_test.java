// org/mockitousage/bugs/deepstubs/DeepStubFailingWhenGenricNestedAsRawTypeTest.java
@Test
  public void rawTypeWithSingleGenericMethod() {
    class Generic<T> {
      T getValue() { return null; }
    }
    Generic raw = mock(Generic.class, RETURNS_DEEP_STUBS);
    when(raw.getValue()).thenReturn("something");
  }
