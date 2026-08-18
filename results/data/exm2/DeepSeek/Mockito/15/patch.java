    public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        if(mocks.size() == 1) {
            final Object matchingMock = mocks.iterator().next();

            return new OngoingInjecter() {
                public boolean thenInject() {
                    try {
                        // Try property setter first
                        String fieldName = field.getName();
                        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        try {
                            java.lang.reflect.Method setter = fieldInstance.getClass().getMethod(setterName, field.getType());
                            setter.invoke(fieldInstance, matchingMock);
                        } catch (NoSuchMethodException e) {
                            // Fall back to field setter
                            new FieldSetter(fieldInstance, field).set(matchingMock);
                        }
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