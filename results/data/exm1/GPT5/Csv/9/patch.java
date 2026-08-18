<M extends Map<String, String>> M putIn(final M map) {
        for (final Entry<String, Integer> entry : mapping.entrySet()) {
            final Integer idxObj = entry.getValue();
            final int col = idxObj != null ? idxObj.intValue() : -1;
            final String key = entry.getKey();
            if (col >= 0 && col < values.length) {
                map.put(key, values[col]);
            } else {
                map.put(key, null);
            }
        }
        return map;
    }