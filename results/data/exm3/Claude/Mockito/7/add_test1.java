// org/mockitousage/bugs/deepstubs/DeepStubFailingWhenGenricNestedAsRawTypeTest.java
@Test
public void discoverDeepMockingWithMultipleLevelNesting() {
    MyClass1 myMock1 = mock(MyClass1.class, RETURNS_DEEP_STUBS);
    MyClass3 deepNested = myMock1.getNested().getNested();
    assertNotNull(deepNested);
    when(deepNested.returnSomething()).thenReturn("Deep Level");
    assertEquals("Deep Level", myMock1.getNested().getNested().returnSomething());
}