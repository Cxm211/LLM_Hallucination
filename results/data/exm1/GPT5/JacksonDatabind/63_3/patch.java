public String getDescription() {
            if (_desc == null) {
                StringBuilder sb = new StringBuilder();

                if (_from == null) { // can this ever occur?
                    sb.append("UNKNOWN");
                } else {
                    Class<?> cls = (_from instanceof Class<?>) ? (Class<?>)_from : _from.getClass();
                    // Include enclosing classes properly; avoid ugly array type names
                    String name;
                    if (cls.isArray()) {
                        int dims = 0;
                        Class<?> comp = cls;
                        while (comp.isArray()) {
                            comp = comp.getComponentType();
                            ++dims;
                        }
                        StringBuilder nb = new StringBuilder(comp.getName());
                        for (int i = 0; i < dims; ++i) {
                            nb.append("[]");
                        }
                        name = nb.toString();
                    } else {
                        name = cls.getName();
                    }
                    sb.append(name);
                }
                sb.append('[');
                if (_fieldName != null) {
                    sb.append('"');
                    sb.append(_fieldName);
                    sb.append('"');
                } else if (_index >= 0) {
                    sb.append(_index);
                } else {
                    sb.append('?');
                }
                sb.append(']');
                _desc = sb.toString();
            }
            return _desc;
        }