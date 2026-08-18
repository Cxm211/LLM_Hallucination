    protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
        // We default to 'false', i.e. not ignorable
        if (status == null) {
            status = false;
        }
        ignoredTypes.put(type, status);
        return status.booleanValue(); 
    }