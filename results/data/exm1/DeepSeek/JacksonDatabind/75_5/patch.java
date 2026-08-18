public static EnumSerializer construct(Class<?> enumClass, SerializationConfig config,
            BeanDescription beanDesc, JsonFormat.Value format)
    {
        Boolean serializeAsIndex = _isShapeWrittenUsingIndex(enumClass, format, true);
        JsonFormat.Shape shape = (format == null) ? null : format.getShape();
        EnumValues v;
        if (shape == Shape.STRING || shape == Shape.NATURAL) {
            v = EnumValues.constructFromToString(config, (Class<Enum<?>>) enumClass);
        } else {
            v = EnumValues.constructFromName(config, (Class<Enum<?>>) enumClass);
        }
        return new EnumSerializer(v, serializeAsIndex);
    }