public String getDescription() {
    if (_desc == null) {
        StringBuilder sb = new StringBuilder();

        if (_from == null) {
            sb.append("UNKNOWN");
        } else {
            Class<?> cls = (_from instanceof Class<?>) ? (Class<?>)_from : _from.getClass();
            String name = cls.getName();
            if (name.startsWith("java.lang.")) {
                name = cls.getSimpleName();
            } else {
                String pkgName = com.fasterxml.jackson.databind.util.ClassUtil.getPackageName(cls);
                if (pkgName != null) {
                    sb.append(pkgName);
                    sb.append('.');
                }
                String simpleName = cls.getSimpleName();
                name = (simpleName == null || simpleName.isEmpty()) ? cls.getName() : simpleName;
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