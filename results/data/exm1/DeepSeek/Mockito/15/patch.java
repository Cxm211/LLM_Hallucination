public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
    List<Object> matchingMocks = new ArrayList<Object>();
    for (Object mock : mocks) {
        if (field.getType().isAssignableFrom(mock.getClass())) {
            matchingMocks.add(mock);
        }
    }
    if(matchingMocks.size() == 1) {
        final Object matchingMock = matchingMocks.iterator().next();
        return new OngoingInjecter() {
            public boolean thenInject() {
                try {
                    new FieldSetter(fieldInstance, field).set(matchingMock);
                } catch (Exception e) {
                    throw new MockitoException("Problems injecting dependency in " + field.getName(), e);
                }
                return true;
            }
        };
    }

    return new OngoingInjecter() {
        public boolean thenInject() {
            return false;
        }
    };
}