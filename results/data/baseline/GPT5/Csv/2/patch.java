public String get(final String name) {
        if (mapping == null) {
            throw new IllegalStateException(
                    "No header mapping was specified, the record values can't be accessed by name");
        }
        final Integer index = mapping.get(name);
        if (index == null) {
            return null;
        }
        final int i = index.intValue();
        return (i >= 0 && values != null && i < values.length) ? values[i] : null;
    }