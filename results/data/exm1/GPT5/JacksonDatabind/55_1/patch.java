public static JsonSerializer<Object> getFallbackKeySerializer(SerializationConfig config,
            Class<?> rawKeyType)
    {
        if (rawKeyType == null || rawKeyType == Object.class) {
            return new Dynamic();
        }
        if (rawKeyType == Enum.class) {
            return new Dynamic();
        }
        if (rawKeyType.isEnum()) {
            return new Default(Default.TYPE_ENUM, rawKeyType);
        }
        return DEFAULT_KEY_SERIALIZER;
    }