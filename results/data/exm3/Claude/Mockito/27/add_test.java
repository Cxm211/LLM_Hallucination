// org/mockitousage/bugs/ListenersLostOnResetMockTest.java
@Test
public void multipleListeners() throws Exception {
    InvocationListener listener1 = mock(InvocationListener.class);
    InvocationListener listener2 = mock(InvocationListener.class);

    List mockedList = mock(List.class, withSettings().invocationListeners(listener1, listener2));
    reset(mockedList);

    mockedList.add("test");

    verify(listener1).reportInvocation(any(MethodInvocationReport.class));
    verify(listener2).reportInvocation(any(MethodInvocationReport.class));
}