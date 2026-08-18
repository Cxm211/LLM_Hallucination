public void process(Class<?> context, Object testClass) {
        Field[] fields = context.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Spy.class)) {
                assertNoAnnotations(Spy.class, field, Mock.class, org.mockito.MockitoAnnotations.Mock.class, Captor.class);
                boolean wasAccessible = field.isAccessible();
                field.setAccessible(true);
                try {
                    Object instance = field.get(testClass);
                    if (instance == null) {
                        try {
                            Object newInstance = field.getType().newInstance();
                            field.set(testClass, Mockito.spy(newInstance));
                        } catch (Exception e) {
                            throw new MockitoException("Cannot create a @Spy for '" + field.getName() + "' field because the *instance* is missing and Mockito cannot instantiate the field.\n" +
                                      "Please ensure the field has a no-arg constructor or initialize the field before initMocks();", e);
                        }
                    } else if (new MockUtil().isMock(instance)) {
                        // instance has been spied earlier
                        Mockito.reset(instance);
                    } else {
                        field.set(testClass, Mockito.spy(instance));
                    }
                } catch (IllegalAccessException e) {
                    throw new MockitoException("Problems initiating spied field " + field.getName(), e);
                } finally {
                    field.setAccessible(wasAccessible);
                }
            }
        }
    }