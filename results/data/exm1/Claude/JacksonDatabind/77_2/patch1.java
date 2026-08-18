protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        if (desc == null) {
            return false;
        }
        status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
        return (status == null) ? false : status.booleanValue();
    }