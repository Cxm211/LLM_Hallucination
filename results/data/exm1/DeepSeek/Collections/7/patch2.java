private void addPropertyInternal(String key, Object value) {
    Object current = this.get(key);
    if (current instanceof String) {
        List values = new Vector(2);
        values.add(current);
        if (value instanceof String) {
            String s = (String) value;
            int start = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\\\\' && i + 1 < s.length() && s.charAt(i + 1) == ',') {
                    i++;
                } else if (c == ',') {
                    values.add(s.substring(start, i));
                    start = i + 1;
                }
            }
            values.add(s.substring(start));
        } else {
            values.add(value);
        }
        put(key, values);
    } else if (current instanceof List) {
        if (value instanceof String) {
            String s = (String) value;
            int start = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\\\\' && i + 1 < s.length() && s.charAt(i + 1) == ',') {
                    i++;
                } else if (c == ',') {
                    ((List) current).add(s.substring(start, i));
                    start = i + 1;
                }
            }
            ((List) current).add(s.substring(start));
        } else {
            ((List) current).add(value);
        }
    } else {
        if (!containsKey(key)) {
            keysAsListed.add(key);
        }
        if (value instanceof String) {
            String s = (String) value;
            int start = 0;
            List parts = new Vector();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\\\\' && i + 1 < s.length() && s.charAt(i + 1) == ',') {
                    i++;
                } else if (c == ',') {
                    parts.add(s.substring(start, i));
                    start = i + 1;
                }
            }
            parts.add(s.substring(start));
            if (parts.size() == 1) {
                put(key, parts.get(0));
            } else {
                put(key, parts);
            }
        } else {
            put(key, value);
        }
    }
}