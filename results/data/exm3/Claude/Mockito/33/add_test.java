// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java
@Test
public void shouldVerifyMultipleCallsWithMixedCasts() {
    iterable.iterator();
    ((Iterable) iterable).iterator();
    iterable.iterator();
    
    verify(iterable, times(3)).iterator();
}