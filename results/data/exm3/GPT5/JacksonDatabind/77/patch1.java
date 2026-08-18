protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        status = (ai == null) ? null : ai.isIgnorableType(desc.getClassInfo());
        // Cache the result to avoid repeated introspection
        Boolean toStore = (status == null) ? Boolean.FALSE : status;
        ignoredTypes.put(type, toStore);
        // We default to 'false', i.e. not ignorable
        return toStore.booleanValue(); 
    }