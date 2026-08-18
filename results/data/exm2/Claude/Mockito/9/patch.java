public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMethod().isAbstract()) {
        return Answers.RETURNS_DEFAULTS.answer(invocation);
    }
    return invocation.callRealMethod();
}