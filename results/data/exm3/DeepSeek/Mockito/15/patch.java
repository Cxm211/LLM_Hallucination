    public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        Object candidate = null;
        if (mocks.size() == 1) {
            Object onlyMock = mocks.iterator().next();
            if (nameMatches(field, onlyMock)) {
                candidate = onlyMock;
            }
        } else {
            for (Object mock : mocks) {
                if (nameMatches(field, mock)) {
                    candidate = mock;
                    break;
                }
            }
        }
        if (candidate != null) {
            final Object matchingMock = candidate;
            return new OngoingInjecter() {
                public boolean thenInject() {
                    try {
                        new org.mockito.internal.util.reflection.PropertySetter(fieldInstance, field).set(matchingMock);
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