private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata, boolean serializable) {
        MockSettings mockSettings = returnTypeGenericMetadata.hasRawExtraInterfaces() ?
                withSettings().extraInterfaces(returnTypeGenericMetadata.rawExtraInterfaces())
                : withSettings();

        if (serializable) {
            mockSettings = mockSettings.serializable();
        }
        return mockSettings.defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }