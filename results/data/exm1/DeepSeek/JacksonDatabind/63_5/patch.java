public String getDescription() {
    if (_desc == null) {
        StringBuilder sb = new StringBuilder();
        if (_from == null) {
            sb.append("UNKNOWN");
        } else {
            Class<?> cls = (_from instanceof Class<?>) ? (Class<?>)_from : _from.getClass();
            String pkgName = com.fasterxml.jackson.databind.util.ClassUtil.getPackageName(cls);
            if (pkgName != null) {
                sb.append(pkgName);
                sb.append('.');
            }
            // fix: include enclosing class name for inner classes
            String clsName = cls.getName();
            if (pkgName != null) {
                clsName = clsName.substring(pkgName.length() + 1);
            }
            sb.append(clsName.replace('$', '.'));
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