protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
        if (status != null) {
            ignoredTypes.put(type, status);
        }
        // We default to 'false', i.e. not ignorable
        return (status == null) ? false : status.booleanValue(); 
    }