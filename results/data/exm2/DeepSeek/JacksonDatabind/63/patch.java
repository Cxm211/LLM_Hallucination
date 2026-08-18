        public String getDescription() {
            if (_desc == null) {
                StringBuilder sb = new StringBuilder();

                if (_from == null) { // can this ever occur?
                    sb.append("UNKNOWN");
                } else {
                    Class<?> cls = (_from instanceof Class<?>) ? (Class<?>)_from : _from.getClass();
                    // Handle arrays nicely, but keep full name for inner classes
                    if (cls.isArray()) {
                        // Count dimensions
                        int dim = 0;
                        Class<?> comp = cls;
                        while (comp.isArray()) {
                            dim++;
                            comp = comp.getComponentType();
                        }
                        sb.append(comp.getName());
                        for (int i = 0; i < dim; i++) {
                            sb.append("[]");
                        }
                    } else {
                        sb.append(cls.getName());
                    }
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