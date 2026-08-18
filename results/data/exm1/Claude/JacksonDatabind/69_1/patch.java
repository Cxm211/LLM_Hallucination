public void addDelegatingCreator(AnnotatedWithParams creator, boolean explicit,
            SettableBeanProperty[] injectables)
    {
        if (creator.getParameterType(0).isCollectionLikeType()) {
            verifyNonDup(creator, C_ARRAY_DELEGATE, explicit);
            _arrayDelegateCreator = _fixAccess(creator);
            _arrayDelegateArgs = injectables;
        } else {
            verifyNonDup(creator, C_DELEGATE, explicit);
            _delegateCreator = _fixAccess(creator);
            _delegateArgs = injectables;
        }
    }