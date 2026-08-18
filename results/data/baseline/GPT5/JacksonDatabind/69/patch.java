public void addDelegatingCreator(AnnotatedWithParams creator, boolean explicit,
            SettableBeanProperty[] injectables)
    {
        if (creator.getParameterType(0).isCollectionLikeType() || creator.getParameterType(0).isMapLikeType()) {
            verifyNonDup(creator, C_ARRAY_DELEGATE, explicit);
            _arrayDelegateArgs = injectables;
        } else {
            verifyNonDup(creator, C_DELEGATE, explicit);
            _delegateArgs = injectables;
        }
    }