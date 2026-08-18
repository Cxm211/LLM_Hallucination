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
            if (pkgName != null) {
                sb.append(pkgName);
                sb.append('.');
            }
            String simpleName = cls.getSimpleName();
            if (simpleName == null || simpleName.isEmpty()) {
                simpleName = cls.getName();
                int idx = simpleName.lastIndexOf('.');
                if (idx >= 0) {
                    simpleName = simpleName.substring(idx + 1);
                }
            }
            sb.append(simpleName);
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