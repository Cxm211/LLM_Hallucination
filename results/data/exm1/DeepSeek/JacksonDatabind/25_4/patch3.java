    public JavaType findTypeMapping(DeserializationConfig config, JavaType type)
    {
        Class<?> src = type.getRawClass();
        Class<?> dst = _mappings.get(new ClassKey(src));
        if (dst == null) {
            return null;
        }
        return type.narrowBy(dst);
    }