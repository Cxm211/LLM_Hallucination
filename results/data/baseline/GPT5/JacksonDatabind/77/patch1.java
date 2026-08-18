protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
        if (status == null) {
            status = Boolean.FALSE;
        }
        ignoredTypes.put(type, status);
        return status.booleanValue(); 
    }