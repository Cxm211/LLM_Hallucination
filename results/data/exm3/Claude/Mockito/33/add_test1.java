// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java
@Test
public void shouldStubbingWorkWithRawTypeFirst() {
    Mockito.when(((Iterable) iterable).iterator()).thenReturn(myIterator);
    Assert.assertNotNull(iterable.iterator());
    Assert.assertNotNull(((Iterable) iterable).iterator());
}