    private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) throws Throwable {
        class SerializableAnswer implements Answer<Object>, java.io.Serializable {
            private final Object target;
            SerializableAnswer(Object target) {
                this.target = target;
            }
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return target;
            }
        }
        container.addAnswer(new SerializableAnswer(mock), false);
        return mock;
    }