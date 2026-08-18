// org/mockitousage/matchers/NewMatchersTest.java
@Test
public void shouldAllowAnySetOfWithNullValue() {
    when(mock.forSet(anySetOf(String.class))).thenReturn("matched");
    
    assertEquals("matched", mock.forSet(new HashSet<String>()));
    assertEquals(null, mock.forSet(null));

    verify(mock, times(1)).forSet(anySetOf(String.class));
}