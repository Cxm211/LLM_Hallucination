private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata) {
        Class<?>[] extra = returnTypeGenericMetadata != null ? returnTypeGenericMetadata.rawExtraInterfaces() : null;
        MockSettings mockSettings =
                (extra != null && extra.length > 0) ?
                withSettings().extraInterfaces(extra)
                : withSettings();

        return mockSettings
                .defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }