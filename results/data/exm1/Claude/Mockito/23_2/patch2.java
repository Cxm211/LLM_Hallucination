private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) {

    container.addAnswer(new Answer<Object>() {
        public Object answer(InvocationOnMock invocation) throws Throwable {
            return mock;
        }
    }, false);

    return mock;
}