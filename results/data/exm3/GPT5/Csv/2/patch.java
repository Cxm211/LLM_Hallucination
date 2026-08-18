public String get(final String name) {
        if (mapping == null) {
            throw new IllegalStateException(
                    "No header mapping was specified, the record values can't be accessed by name");
        }
        final Integer index = mapping.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Mapping for " + name + " not found");
        }
        final int i = index.intValue();
        if (i < 0 || i >= values.length) {
            throw new IllegalStateException("Index for header '" + name + "' out of bounds: " + i);
        }
        return values[i];
    }