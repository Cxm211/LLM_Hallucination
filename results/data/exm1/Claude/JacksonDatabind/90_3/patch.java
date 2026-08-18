public boolean canInstantiate() {
    return canCreateUsingDefault()
            || canCreateUsingDelegate() 
            || canCreateFromString()
            || canCreateFromInt() || canCreateFromLong()
            || canCreateFromDouble() || canCreateFromBoolean();
}