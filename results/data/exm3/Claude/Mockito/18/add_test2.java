// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java
@Test
public void should_return_empty_sorted_map() throws Exception {
    assertTrue(((SortedMap) values.returnValueFor(SortedMap.class)).isEmpty());
}