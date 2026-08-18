// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java
@Test
public void should_return_empty_sorted_set() throws Exception {
    assertTrue(((SortedSet) values.returnValueFor(SortedSet.class)).isEmpty());
}