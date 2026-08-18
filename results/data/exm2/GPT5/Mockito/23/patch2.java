private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) throws Throwable {

        container.addAnswer(new org.mockito.internal.stubbing.answers.Returns(mock), false);

        return mock;
    }