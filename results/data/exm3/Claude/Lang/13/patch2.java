protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    String name = desc.getName();
    try {
        return Class.forName(name, false, classLoader);
    } catch (ClassNotFoundException ex) {
        try {
            return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex2) {
            Class<?> cls = resolvePrimitiveClass(name);
            if (cls != null) {
                return cls;
            }
            throw ex2;
        }
    }
}

private Class<?> resolvePrimitiveClass(String name) {
    if ("int".equals(name)) return int.class;
    if ("byte".equals(name)) return byte.class;
    if ("short".equals(name)) return short.class;
    if ("long".equals(name)) return long.class;
    if ("float".equals(name)) return float.class;
    if ("double".equals(name)) return double.class;
    if ("boolean".equals(name)) return boolean.class;
    if ("char".equals(name)) return char.class;
    if ("void".equals(name)) return void.class;
    return null;
}