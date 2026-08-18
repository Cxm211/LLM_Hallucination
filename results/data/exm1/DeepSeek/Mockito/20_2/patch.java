public <T> T createMock(MockCreationSettings<T> settings, MockHandler handler) {
        if (settings.getSerializableMode() == SerializableMode.ACROSS_CLASSLOADERS) {
            throw new MockitoException("Serialization across classloaders not yet supported with ByteBuddyMockMaker");
        }
        Class<? extends T> mockedProxyType = cachingMockBytecodeGenerator.get(
                settings.getTypeToMock(),
                settings.getExtraInterfaces()
        );
        T mockInstance = null;
        try {
            mockInstance = classInstantiator.instantiate(mockedProxyType);
            MockMethodInterceptor.MockAccess mockAccess = (MockMethodInterceptor.MockAccess) mockInstance;
            mockAccess.setMockitoInterceptor(new MockMethodInterceptor(asInternalMockHandler(handler), settings));

            return ensureMockIsAssignableToMockedType(settings, mockInstance);
        } catch (ClassCastException cce) {
            throw new MockitoException(join(
                    "ClassCastException occurred while creating the mockito mock :",
                    "  class to mock : " + describeClass(mockedProxyType),
                    "  created class : " + describeClass(settings.getTypeToMock()),
                    "  proxy instance class : " + describeClass(mockInstance),
                    "  instance creation by : " + classInstantiator.getClass().getSimpleName(),
                    "",
                    "You might experience classloading issues, please ask the mockito mailing-list.",
                    ""
            ),cce);
        } catch (org.mockito.internal.creation.instance.InstantiationException e) {
            String message = "Unable to create mock instance of type '" + mockedProxyType.getSuperclass().getSimpleName() + "'";
            if (settings.getSerializableMode() != SerializableMode.NONE) {
                message += "\n" +
                        "If you have used the serializable() option, please ensure that the type implements Serializable and has a no-arg constructor.";
            }
            throw new MockitoException(message, e);
        }
    }