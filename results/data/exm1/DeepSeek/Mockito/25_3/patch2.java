private Object recordDeepStubMock(InvocationOnMock invocation, InvocationContainerImpl container, Class<?> rawType) {
    final Object mock = org.mockito.Mockito.mock(rawType, this);

    container.addAnswer(new Answer<Object>() {
        public Object answer(InvocationOnMock invocation) throws Throwable {
            return mock;
        }
    }, false);

    return mock;
}