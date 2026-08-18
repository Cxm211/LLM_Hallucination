// org/mockito/exceptions/ReporterTest.java
@Test(expected = NoInteractionsWanted.class)
public void should_handle_mock_toString_throwing_exception_in_noMoreInteractionsWanted() throws Exception {
    IMethods mock = mock(IMethods.class, new Answer() {
        public Object answer(InvocationOnMock invocation) throws Throwable {
            if (invocation.getMethod().getName().equals("toString")) {
                throw new RuntimeException("toString not available");
            }
            return null;
        }
    });
    Invocation invocation = new InvocationBuilder().mock(mock).toInvocation();
    new Reporter().noMoreInteractionsWanted(invocation, Collections.<VerificationAwareInvocation>emptyList());
}