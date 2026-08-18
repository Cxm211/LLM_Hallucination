protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        // 21-Apr-2016, tatu: For 2.8, can specify config overrides
        ConfigOverride override = config.findConfigOverride(type);
        if (override != null) {
            status = override.getIsIgnoredType();
        }
        if (status == null) {
            BeanDescription desc = config.introspectClassAnnotations(type);
            AnnotationIntrospector ai = config.getAnnotationIntrospector();
            if (ai != null) {
                status = ai.isIgnorableType(desc.getClassInfo());
            }
            // We default to 'false', i.e. not ignorable
            if (status == null) {
                status = Boolean.FALSE;
            }
        }
        ignoredTypes.put(type, status);
        return status.booleanValue();
    }