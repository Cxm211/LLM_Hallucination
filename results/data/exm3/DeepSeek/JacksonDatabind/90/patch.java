    public boolean canInstantiate() {
        return canCreateUsingDefault()
                || canCreateUsingDelegate() 
                || canCreateFromObjectWith() || canCreateFromString()
                || canCreateFromInt() || canCreateFromLong()
                || canCreateFromDouble() || canCreateFromBoolean()
                || (_arrayDelegateCreator != null);
    }