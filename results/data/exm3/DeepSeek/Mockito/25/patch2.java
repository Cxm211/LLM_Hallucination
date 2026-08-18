    private Object recordDeepStubMock(InvocationOnMock invocation, InvocationContainerImpl container, GenericMetadataSupport returnTypeGenericMetadata) {
        Class<?> rawType = returnTypeGenericMetadata.rawType();
        List<Type> extraInterfaces = returnTypeGenericMetadata.extraInterfaces();
        Class<?>[] extraInterfacesArray = new Class<?>[extraInterfaces.size()];
        int i = 0;
        for (Type type : extraInterfaces) {
            if (type instanceof Class) {
                extraInterfacesArray[i++] = (Class<?>) type;
            }
        }
        if (i < extraInterfacesArray.length) {
            extraInterfacesArray = Arrays.copyOf(extraInterfacesArray, i);
        }
        
        MockSettings settings = org.mockito.Mockito.withSettings()
                .defaultAnswer(this)
                .extraInterfaces(extraInterfacesArray);
        final Object mock = org.mockito.Mockito.mock(rawType, settings);

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }