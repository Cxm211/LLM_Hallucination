// org/mockitousage/matchers/NewMatchersTest.java
@Test
public void shouldAllowAnyMapOfWithNullValue() {
    when(mock.forMap(anyMapOf(String.class, String.class))).thenReturn("matched");
    
    assertEquals("matched", mock.forMap(new HashMap<String, String>()));
    assertEquals(null, mock.forMap(null));

    verify(mock, times(1)).forMap(anyMapOf(String.class, String.class));
}