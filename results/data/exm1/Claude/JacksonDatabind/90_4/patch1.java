public boolean canCreateFromObjectWith() {
    if (_withArgsCreator == null) {
        return false;
    }
    SettableBeanProperty[] properties = _withArgsCreator.properties();
    return (properties != null && properties.length > 0);
}