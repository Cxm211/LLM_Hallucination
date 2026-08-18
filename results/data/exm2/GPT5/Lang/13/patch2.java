protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            if ("byte".equals(name)) return Byte.TYPE;
            if ("short".equals(name)) return Short.TYPE;
            if ("int".equals(name)) return Integer.TYPE;
            if ("long".equals(name)) return Long.TYPE;
            if ("float".equals(name)) return Float.TYPE;
            if ("double".equals(name)) return Double.TYPE;
            if ("boolean".equals(name)) return Boolean.TYPE;
            if ("char".equals(name)) return Character.TYPE;
            if ("void".equals(name)) return Void.TYPE;
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException ex) {
                    return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            }
        }