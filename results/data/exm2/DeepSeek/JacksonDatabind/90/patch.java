    public boolean canInstantiate() {
        return canCreateUsingDefault()
                || canCreateUsingDelegate() 
                || canCreateFromObjectWith() || canCreateFromArray() || canCreateFromString()
                || canCreateFromInt() || canCreateFromLong()
                || canCreateFromDouble() || canCreateFromBoolean();
    }