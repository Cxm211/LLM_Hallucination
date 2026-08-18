public String getDescription() {
            if (_desc == null) {
                StringBuilder sb = new StringBuilder();

                if (_from == null) { // can this ever occur?
                    sb.append("UNKNOWN");
                } else {
                    Class<?> cls = (_from instanceof Class<?>) ? (Class<?>)_from : _from.getClass();
                    String pkgName = com.fasterxml.jackson.databind.util.ClassUtil.getPackageName(cls);
                    String name = cls.getName();
                    if (pkgName != null && pkgName.length() > 0) {
                        sb.append(pkgName);
                        sb.append('.');
                        if (name.startsWith(pkgName)) {
                            int ix = pkgName.length();
                            if (ix < name.length() && name.charAt(ix) == '.') {
                                name = name.substring(ix + 1);
                            }
                        }
                    }
                    sb.append(name.replace('$', '.'));
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