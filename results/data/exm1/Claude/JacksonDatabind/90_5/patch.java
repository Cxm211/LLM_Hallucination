public boolean canInstantiate() {
    return canCreateUsingDefault()
            || canCreateUsingDelegate() 
            || canCreateFromObjectWith() || canCreateFromString()
            || canCreateFromInt() || canCreateFromLong()
            || canCreateFromDouble() || canCreateFromBoolean();
}

public boolean canCreateFromObjectWith() {
    return (_withArgsCreator != null);
}