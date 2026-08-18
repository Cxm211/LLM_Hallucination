// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java
@Test public void should_return_nonzero_if_mock_is_compared_to_different_object() {
    //given
    Date d1 = mock(Date.class);
    Date d2 = mock(Date.class);
    d1.compareTo(d2);
    Invocation compareTo = this.getLastInvocation();

    //when
    Object result = values.answer(compareTo);

    //then
    assertNotEquals(0, result);
}