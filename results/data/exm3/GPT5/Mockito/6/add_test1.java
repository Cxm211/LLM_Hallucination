// org/mockitousage/matchers/NewMatchersTest.java::shouldNotAcceptNullInAnyListOfMatcher
@Test
public void shouldNotAcceptNullInAnyListOfMatcher() {
    when(mock.forList(anyListOf(String.class))).thenReturn("matched");

    assertEquals("matched", mock.forList(Arrays.asList("a")));
    assertEquals(null, mock.forList(null));

    verify(mock, times(1)).forList(anyListOf(String.class));
}