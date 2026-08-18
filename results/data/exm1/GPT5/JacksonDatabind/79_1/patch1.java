protected ObjectIdInfo(PropertyName prop, Class<?> scope, Class<? extends ObjectIdGenerator<?>> gen,
            boolean alwaysAsId, Class<? extends ObjectIdResolver> resolver)
    {
        _propertyName = prop;
        _scope = scope;
        _generator = gen;
        _alwaysAsId = alwaysAsId;
        if (resolver == null) {
            resolver = SimpleObjectIdResolver.class;
        }
        _resolver = resolver;
    }

    // Added constructor to support usage without explicit 'alwaysAsId' parameter
    protected ObjectIdInfo(PropertyName prop, Class<?> scope, Class<? extends ObjectIdGenerator<?>> gen,
            Class<? extends ObjectIdResolver> resolver)
    {
        this(prop, scope, gen, false, resolver);
    }