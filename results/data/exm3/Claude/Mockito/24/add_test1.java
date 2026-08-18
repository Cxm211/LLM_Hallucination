// org/mockitousage/bugs/ShouldMocksCompareToBeConsistentWithEqualsTest.java
@Test
public void should_compare_to_return_nonzero_when_comparing_different_mocks() {
    //given
    Date today = mock(Date.class);
    Date tomorrow = mock(Date.class);

    //when
    Set<Date> set = new TreeSet<Date>();
    set.add(today);
    set.add(tomorrow);

    //then
    assertEquals(2, set.size());
}