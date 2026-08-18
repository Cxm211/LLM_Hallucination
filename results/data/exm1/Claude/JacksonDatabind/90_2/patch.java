public boolean canInstantiate() {
    return canCreateUsingDefault()
            || canCreateUsingDelegate() 
            || (canCreateFromObjectWith() && hasPropertiesBasedCreator())
            || canCreateFromString()
            || canCreateFromInt() || canCreateFromLong()
            || canCreateFromDouble() || canCreateFromBoolean();
}

private boolean hasPropertiesBasedCreator() {
    if (_withArgsCreator == null) {
        return false;
    }
    for (SettableBeanProperty prop : _withArgsCreator.properties()) {
        if (prop != null) {
            return true;
        }
    }
    return false;
}