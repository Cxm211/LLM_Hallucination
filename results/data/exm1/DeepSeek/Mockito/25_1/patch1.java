private Object getMock(InvocationOnMock invocation, Class<?> rawType) throws Throwable {
    InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();
    for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
        if (stubbedInvocationMatcher.matches(invocation)) {
            return stubbedInvocationMatcher.answer(invocation);
        }
    }
    return recordDeepStubMock(invocation, container, rawType);
}