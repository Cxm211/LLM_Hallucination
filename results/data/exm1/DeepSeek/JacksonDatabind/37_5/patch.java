    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Need to re-resolve to ensure correct type bindings for the subclass
        return TypeFactory.defaultInstance().constructSpecializedType(this, subclass);
    }