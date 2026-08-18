protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        return com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance()
                .constructSpecializedType(this, subclass);
    }