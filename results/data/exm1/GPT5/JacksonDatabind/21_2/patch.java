public boolean isAnnotationBundle(Annotation ann) {
        if (ann == null) {
            return false;
        }
        // Check direct and recursive meta-annotations for @JacksonAnnotationsInside
        java.util.Set<Class<?>> visited = new java.util.HashSet<Class<?>>();
        java.util.ArrayDeque<Class<?>> stack = new java.util.ArrayDeque<Class<?>>();
        Class<?> root = ann.annotationType();
        stack.push(root);
        while (!stack.isEmpty()) {
            Class<?> curr = stack.pop();
            if (!visited.add(curr)) {
                continue;
            }
            if (curr.getAnnotation(JacksonAnnotationsInside.class) != null) {
                return true;
            }
            for (Annotation meta : curr.getAnnotations()) {
                Class<?> mt = meta.annotationType();
                // Skip core meta-annotations and self-references
                if (mt == curr) {
                    continue;
                }
                String pkg = mt.getName();
                if (pkg.startsWith("java.lang.annotation.")) {
                    continue;
                }
                if (!visited.contains(mt)) {
                    stack.push(mt);
                }
            }
        }
        return false;
    }