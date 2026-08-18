public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        if (mocks.size() == 1) {
            final Object matchingMock = mocks.iterator().next();

            return new OngoingInjecter() {
                public boolean thenInject() {
                    try {
                        // try property setter first
                        String fieldName = field.getName();
                        if (fieldName != null && fieldName.length() > 0) {
                            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                            for (java.lang.reflect.Method m : fieldInstance.getClass().getMethods()) {
                                if (m.getName().equals(setterName) && m.getParameterTypes().length == 1) {
                                    Class<?> paramType = m.getParameterTypes()[0];
                                    if (paramType.isInstance(matchingMock)) {
                                        m.invoke(fieldInstance, matchingMock);
                                        return true;
                                    }
                                }
                            }
                        }
                        // fallback to direct field injection
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