        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            // Handle primitive types
            if (name.equals("byte")) return byte.class;
            if (name.equals("short")) return short.class;
            if (name.equals("int")) return int.class;
            if (name.equals("long")) return long.class;
            if (name.equals("float")) return float.class;
            if (name.equals("double")) return double.class;
            if (name.equals("boolean")) return boolean.class;
            if (name.equals("char")) return char.class;
            if (name.equals("void")) return void.class;
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException ex) {
                return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            }
        }