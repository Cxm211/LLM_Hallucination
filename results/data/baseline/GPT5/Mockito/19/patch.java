public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        // First filter by type
        List<Object> mockTypeMatches = new ArrayList<Object>();
        for (Object mock : mocks) {
            if (field.getType().isAssignableFrom(mock.getClass())) {
                mockTypeMatches.add(mock);
            }
        }
        Collection<Object> candidates = mockTypeMatches.isEmpty() ? new ArrayList<Object>(mocks) : mockTypeMatches;

        // Then, if multiple, filter by name
        if (candidates.size() > 1) {
            List<Object> mockNameMatches = new ArrayList<Object>();
            for (Object mock : candidates) {
                if (field.getName().equals(mockUtil.getMockName(mock).toString())) {
                    mockNameMatches.add(mock);
                }
            }
            if (!mockNameMatches.isEmpty()) {
                candidates = mockNameMatches;
            }
        }

        if (candidates.size() == 1) {
            final Object matchingMock = candidates.iterator().next();

            return new OngoingInjecter() {
                public Object thenInject() {
                    try {
                        if (!new BeanPropertySetter(fieldInstance, field).set(matchingMock)) {
                            new FieldSetter(fieldInstance, field).set(matchingMock);
                        }
                    } catch (RuntimeException e) {
                        new Reporter().cannotInjectDependency(field, matchingMock, e);
                    }
                    return matchingMock;
                }
            };
        }

        return new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };

    }