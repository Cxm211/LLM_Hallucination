// org/mockitousage/matchers/NewMatchersTest.java
@Test
public void shouldAllowAnyListOfWithNullValue() {
    when(mock.forList(anyListOf(String.class))).thenReturn("matched");
    
    assertEquals("matched", mock.forList(Arrays.asList("x", "y")));
    assertEquals(null, mock.forList(null));

    verify(mock, times(1)).forList(anyListOf(String.class));
}