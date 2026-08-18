public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt,
            JavaType type, BeanDescription beanDesc)
        throws JsonMappingException
    {
        final DeserializationConfig config = ctxt.getConfig();
        // We may also have custom overrides:
        JsonDeserializer<Object> custom = _findCustomBeanDeserializer(type, config, beanDesc);
        if (custom != null) {
            return custom;
        }
        /* One more thing to check: do we have an exception type
         * (Throwable or its sub-classes)? If so, need slightly
         * different handling.
         */
        if (type.isThrowable()) {
            return buildThrowableDeserializer(ctxt, type, beanDesc);
        }
        /* Or, for abstract types, may have alternate means for resolution
         * (defaulting, materialization)
         */
        // 29-Nov-2015, tatu: Also, filter out calls to primitive types, they are
        //    not something we could materialize anything for
        if (type.isAbstract() && !type.isPrimitive() && !type.isEnumType()) {
            // Let's make it possible to materialize abstract types.
            JavaType concreteType = materializeAbstractType(ctxt, type, beanDesc);
            if (concreteType != null) {
                /* important: introspect actual implementation (abstract class or
                 * interface doesn't have constructors, for one)
                 */
                beanDesc = config.introspect(concreteType);
                return buildBeanDeserializer(ctxt, concreteType, beanDesc);
            }
        }
        // Otherwise, may want to check handlers for standard types, from superclass:
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) findStdDeserializer(ctxt, type, beanDesc);
        if (deser != null) {
            return deser;
        }

        // Otherwise: could the class be a Bean class? If not, bail out
        if (!isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        // For checks like [databind#1599]
        // Use generic bean introspection to build deserializer
        return buildBeanDeserializer(ctxt, type, beanDesc);
    }

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
            status = (ai == null) ? null : ai.isIgnorableType(desc.getClassInfo());
            // We default to 'false', i.e. not ignorable
            if (status == null) {
                status = Boolean.FALSE;
            }
        }
        ignoredTypes.put(type, status);
        return status.booleanValue();
    }