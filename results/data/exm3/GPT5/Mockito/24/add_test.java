// org/mockitousage/bugs/ShouldMocksCompareToBeConsistentWithEqualsTest.java::should_compare_to_be_consistent_with_equals_when_comparing_the_same_reference
@Test
public void should_treemap_treat_same_mock_key_as_equal_by_compareTo() {
    Date today = mock(Date.class);

    Map<Date, Integer> map = new TreeMap<Date, Integer>();
    map.put(today, 1);
    map.put(today, 2);

    assertEquals(1, map.size());
    assertEquals(Integer.valueOf(2), map.get(today));
}