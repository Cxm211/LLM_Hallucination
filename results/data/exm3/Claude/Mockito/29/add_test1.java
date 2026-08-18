// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
public void shouldHandleNullInSameMatcherWhenActualIsNull() {
    mock.objectArgMethod(null);
    verify(mock).objectArgMethod(same(null));
}