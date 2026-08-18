// org/mockitousage/bugs/ListenersLostOnResetMockTest.java
@Test
    public void shouldPreserveDefaultAnswerAfterReset() {
        List mock = mock(List.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        reset(mock);
        MockingDetails details = mockingDetails(mock);
        assertThat(details.getDefaultAnswer(), is(RETURNS_SMART_NULLS));
    }
