protected Object convertKey(Object key) {
    if (key != null) {
        return key.toString().toLowerCase(Locale.ENGLISH);
    } else {
        return AbstractHashedMap.NULL;
    }
}