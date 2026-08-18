// org/mockitousage/bugs/ListenersLostOnResetMockTest.java::listener
@Test
public void preserves_default_answer_on_reset() throws Exception {
    InvocationListener invocationListener = mock(InvocationListener.class);

    List mockedList = mock(List.class, withSettings().invocationListeners(invocationListener).defaultAnswer(org.mockito.Answers.RETURNS_SMART_NULLS));
    reset(mockedList);

    Object value = mockedList.get(0);
    assertNotNull(value);

    verify(invocationListener).reportInvocation(any(MethodInvocationReport.class));
}