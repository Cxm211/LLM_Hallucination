        public String getDescription() {
            if (_desc == null) {
                StringBuilder sb = new StringBuilder();

                if (_from == null) { // can this ever occur?
                    sb.append("UNKNOWN");
                } else {
                    Class<?> cls = (_from instanceof Class<?>) ? (Class<?>)_from : _from.getClass();
                    // Hmmh. Although Class.getName() is mostly ok, it does look
                    // butt-ugly for arrays.
                    // 06-Oct-2016, tatu: as per [databind#1403], `getSimpleName()` not so good
                    //   as it drops enclosing class. So let's try bit different approach
                    String pkgName = com.fasterxml.jackson.databind.util.ClassUtil.getPackageName(cls);
                    if (cls.isArray()) {
                        // For arrays, use getSimpleName() which returns a readable form like "int[]"
                        if (pkgName != null) {
                            sb.append(pkgName);
                            sb.append('.');
                        }
                        sb.append(cls.getSimpleName());
                    } else {
                        // For non-array classes, include the enclosing class names
                        String fullName = cls.getName();
                        if (pkgName != null && pkgName.length() > 0) {
                            // Remove the package prefix
                            fullName = fullName.substring(pkgName.length() + 1);
                        }
                        sb.append(fullName);
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