// org/mockitousage/bugs/deepstubs/DeepStubFailingWhenGenricNestedAsRawTypeTest.java
@Test
public void discoverDeepMockingWithNullActualTypeArgument() {
    MyClass1 myMock1 = mock(MyClass1.class, RETURNS_DEEP_STUBS);
    MyClass2 nested = myMock1.getNested();
    assertNotNull(nested);
}