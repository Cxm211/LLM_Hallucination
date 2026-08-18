protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        // use the provided beanDesc if possible, otherwise introspect
        AnnotatedClass classInfo = (beanDesc != null) ? beanDesc.getClassInfo() : null;
        if (classInfo == null) {
            BeanDescription desc = config.introspectClassAnnotations(type);
            classInfo = desc.getClassInfo();
        }
        status = config.getAnnotationIntrospector().isIgnorableType(classInfo);
        // We default to 'false', i.e. not ignorable
        boolean result = (status == null) ? false : status.booleanValue();
        ignoredTypes.put(type, result);
        return result;
    }