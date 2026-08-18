// org/mockitousage/matchers/NewMatchersTest.java
@Test
public void shouldAllowAnyCollectionOfWithNullValue() {
    when(mock.forCollection(anyCollectionOf(String.class))).thenReturn("matched");
    
    assertEquals("matched", mock.forCollection(Arrays.asList("x", "y")));
    assertEquals(null, mock.forCollection(null));

    verify(mock, times(1)).forCollection(anyCollectionOf(String.class));
}