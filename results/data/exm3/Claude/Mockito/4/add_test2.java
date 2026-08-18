// org/mockito/exceptions/ReporterTest.java
@Test(expected = NoInteractionsWanted.class)
public void should_handle_null_mock_in_noMoreInteractionsWanted() throws Exception {
    Invocation invocation = new InvocationBuilder().mock(null).toInvocation();
    new Reporter().noMoreInteractionsWanted(invocation, Collections.<VerificationAwareInvocation>emptyList());
}