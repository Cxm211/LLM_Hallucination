    private Object newDeepStubMock(GenericMetadataSupport returnTypeGenericMetadata, boolean serializable) {
        return mockitoCore().mock(
                returnTypeGenericMetadata.rawType(),
                withSettingsUsing(returnTypeGenericMetadata, serializable)
        );
    }