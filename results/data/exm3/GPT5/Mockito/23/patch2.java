private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) throws Throwable {

        container.addAnswer(new SerializableReturnsSame(mock), false);

        return mock;
    }

    private static class SerializableReturnsSame implements Answer<Object>, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final Object toReturn;
        SerializableReturnsSame(Object toReturn) { this.toReturn = toReturn; }
        public Object answer(InvocationOnMock invocation) throws Throwable { return toReturn; }
    }