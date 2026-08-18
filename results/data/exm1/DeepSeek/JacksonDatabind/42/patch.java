protected Object _deserializeFromEmptyString() throws IOException {
    if (_kind == STD_URI) {
        return null;
    }
    return super._deserializeFromEmptyString();
}