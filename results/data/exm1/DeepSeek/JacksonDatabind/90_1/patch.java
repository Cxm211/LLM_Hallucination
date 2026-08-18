public boolean canInstantiate() {
        return canCreateUsingDefault()
                || canCreateUsingDelegate() 
                || canCreateUsingObjectWith() || canCreateFromString()
                || canCreateFromInt() || canCreateFromLong()
                || canCreateFromDouble() || canCreateFromBoolean();
    }