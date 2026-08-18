// org/mockito/exceptions/ReporterTest.java
public void noMoreInteractionsWanted_with_non_empty_invocations_and_bogus_default_answer() throws Exception {
    Invocation invocation = new InvocationBuilder().mock(mock(IMethods.class, new Returns(123))).toInvocation();
    List<VerificationAwareInvocation> invocations = Arrays.asList((VerificationAwareInvocation) invocation);
    new Reporter().noMoreInteractionsWanted(invocation, invocations);
}
