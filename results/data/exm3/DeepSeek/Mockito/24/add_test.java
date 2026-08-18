// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java
@Test public void should_return_zero_for_self_comparison_of_comparable_mock() {
        //given
        Comparable<String> mock = mock(Comparable.class);
        mock.compareTo(mock);
        Invocation compareTo = this.getLastInvocation();

        //when
        Object result = values.answer(compareTo);

        //then
        assertEquals(0, result);
    }
