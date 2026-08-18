// buggy function
        private void readTypeVariables() {
            for (Type type : typeVariable.getBounds()) {
                registerTypeVariablesOn(type);
            }
            registerTypeVariablesOn(getActualTypeArgumentFor(typeVariable));
        }

// trigger testcase
// org/mockitousage/bugs/deepstubs/DeepStubFailingWhenGenricNestedAsRawTypeTest.java::discoverDeepMockingOfGenerics
@Test
  public void discoverDeepMockingOfGenerics() {
    MyClass1 myMock1 = mock(MyClass1.class, RETURNS_DEEP_STUBS);
    when(myMock1.getNested().getNested().returnSomething()).thenReturn("Hello World.");
  }
