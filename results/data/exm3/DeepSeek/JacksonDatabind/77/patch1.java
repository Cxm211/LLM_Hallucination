    protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        // Check exact match
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        // Check superclasses
        Class<?> superClass = type.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            status = ignoredTypes.get(superClass);
            if (status != null) {
                if (status.booleanValue()) {
                    return true;
                }
                // if false, we continue
            }
            superClass = superClass.getSuperclass();
        }
        // Check all interfaces (including those from superclasses)
        java.util.Set<Class<?>> visited = new java.util.HashSet<>();
        java.util.Deque<Class<?>> queue = new java.util.ArrayDeque<>();
        // Start with the type and all its superclasses
        Class<?> cls = type;
        while (cls != null && cls != Object.class) {
            queue.add(cls);
            cls = cls.getSuperclass();
        }
        while (!queue.isEmpty()) {
            Class<?> c = queue.removeFirst();
            for (Class<?> iface : c.getInterfaces()) {
                if (visited.add(iface)) {
                    status = ignoredTypes.get(iface);
                    if (status != null && status.booleanValue()) {
                        return true;
                    }
                    queue.addLast(iface); // to process superinterfaces
                }
            }
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
        // We default to 'false', i.e. not ignorable
        return (status == null) ? false : status.booleanValue(); 
    }