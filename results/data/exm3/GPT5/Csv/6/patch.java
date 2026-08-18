<M extends Map<String, String>> M putIn(final M map) {
        for (final Entry<String, Integer> entry : mapping.entrySet()) {
            final int col = entry.getValue().intValue();
            final String value = (col >= 0 && col < values.length) ? values[col] : null;
            map.put(entry.getKey(), value);
        }
        return map;
    }