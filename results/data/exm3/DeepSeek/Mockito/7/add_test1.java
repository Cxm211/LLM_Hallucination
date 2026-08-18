// org/mockitousage/bugs/deepstubs/DeepStubFailingWhenGenricNestedAsRawTypeTest.java
@Test
  public void rawTypeWithNestedGeneric() {
    class Outer<T> {
      Inner<T> getInner() { return null; }
    }
    class Inner<U> {
      U getData() { return null; }
    }
    Outer raw = mock(Outer.class, RETURNS_DEEP_STUBS);
    when(raw.getInner().getData()).thenReturn("data");
  }
