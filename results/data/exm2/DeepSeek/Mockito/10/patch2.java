    private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata) {
        MockSettings mockSettings = returnTypeGenericMetadata.hasRawExtraInterfaces() ?
                withSettings().extraInterfaces(returnTypeGenericMetadata.rawExtraInterfaces())
                : withSettings();

        if (Serializable.class.isAssignableFrom(returnTypeGenericMetadata.rawType())) {
            mockSettings = mockSettings.serializable();
        }
        return mockSettings.defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }