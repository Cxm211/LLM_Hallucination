protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    String name = desc.getName();
    if ("byte".equals(name)) return byte.class;
    if ("short".equals(name)) return short.class;
    if ("int".equals(name)) return int.class;
    if ("long".equals(name)) return long.class;
    if ("float".equals(name)) return float.class;
    if ("double".equals(name)) return double.class;
    if ("boolean".equals(name)) return boolean.class;
    if ("char".equals(name)) return char.class;
    if ("void".equals(name)) return void.class;
    try {
        return Class.forName(name, false, classLoader);
    } catch (ClassNotFoundException ex) {
        try {
            return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex2) {
            return super.resolveClass(desc);
        }
    }
}