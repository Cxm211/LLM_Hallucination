public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        if (mocks.size() == 1) {
            final Object matchingMock = mocks.iterator().next();

            return new OngoingInjecter() {
                public Object thenInject() {
                    boolean injected = false;
                    try {
                        if (new BeanPropertySetter(fieldInstance, field).set(matchingMock)) {
                            injected = true;
                        } else {
                            new FieldSetter(fieldInstance, field).set(matchingMock);
                            injected = true;
                        }
                    } catch (RuntimeException e) {
                        new Reporter().cannotInjectDependency(field, matchingMock, e);
                        return null;
                    }
                    return injected ? matchingMock : null;
                }
            };
        }

        return new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };

    }