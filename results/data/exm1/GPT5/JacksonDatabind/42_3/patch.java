protected Object _deserializeFromEmptyString() throws IOException {
    // As per [databind#398], URI requires special handling: empty String -> null
    if (_kind == STD_URI) {
        return null;
    }
    // As per [databind#1123], Locale too: empty String -> Locale.ROOT
    if (_kind == STD_LOCALE) {
        return java.util.Locale.ROOT;
    }
    return super._deserializeFromEmptyString();
}