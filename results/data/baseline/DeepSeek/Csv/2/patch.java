public String get(final String name) {
    if (mapping == null) {
        throw new IllegalStateException(
                "No header mapping was specified, the record values can't be accessed by name");
    }
    final Integer index = mapping.get(name);
    if (index == null) {
        throw new IllegalArgumentException(String.format("Mapping for %s not found, expected one of %s", name, mapping.keySet()));
    }
    return values[index.intValue()];
}