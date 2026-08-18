    public String get(final String name) {
        if (mapping == null) {
            throw new IllegalStateException(
                    "No header mapping was specified, the record values can't be accessed by name");
        }
        final Integer index = mapping.get(name);
            return index != null ? values[index.intValue()] : null;
    }

// trigger testcase
public void testGetStringInconsistentRecord() {
        header.put("fourth", Integer.valueOf(4));
        recordWithHeader.get("fourth");
    }
