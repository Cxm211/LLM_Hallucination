protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc, Class<?> type, Map<Class<?>,Boolean> ignoredTypes) {
    Boolean status = ignoredTypes.get(type);
    if (status != null) {
        return status.booleanValue();
    }
    ConfigOverride override = config.findConfigOverride(type);
    if (override != null) {
        status = override.getIsIgnoredType();
    }
    if (status == null) {
        status = config.getAnnotationIntrospector().isIgnorableType(beanDesc.getClassInfo());
        if (status == null) {
            status = Boolean.FALSE;
        }
    }
    ignoredTypes.put(type, status);
    return status.booleanValue();
}