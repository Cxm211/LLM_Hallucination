public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        if(mocks.size() == 1) {
            final Object matchingMock = mocks.iterator().next();

            return new OngoingInjecter() {
                public boolean thenInject() {
                    try {
                        // prefer setter if available
                        String name = field.getName();
                        String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                        java.lang.reflect.Method method = null;
                        try {
                            method = fieldInstance.getClass().getMethod(setterName, field.getType());
                        } catch (NoSuchMethodException ignore) {
                            for (java.lang.reflect.Method m : fieldInstance.getClass().getMethods()) {
                                if (m.getName().equals(setterName)
                                    && m.getParameterTypes().length == 1
                                    && m.getParameterTypes()[0].isAssignableFrom(matchingMock.getClass())) {
                                    method = m;
                                    break;
                                }
                            }
                        }
                        if (method != null) {
                            method.setAccessible(true);
                            method.invoke(fieldInstance, matchingMock);
                        } else {
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