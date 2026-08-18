    public static EnumSerializer construct(Class<?> enumClass, SerializationConfig config,
            BeanDescription beanDesc, JsonFormat.Value format)
    {
        /* 08-Apr-2015, tatu: As per [databind#749], we can not statically determine
         *   between name() and toString(), need to construct `EnumValues` with names,
         *   handle toString() case dynamically (for example)
         */
        EnumValues v;
        if (format != null && (format.getShape() == Shape.STRING || format.getShape() == Shape.NATURAL)) {
            v = EnumValues.constructFromToString(config, (Class<Enum<?>>) enumClass);
        } else {
            v = EnumValues.constructFromName(config, (Class<Enum<?>>) enumClass);
        }
        Boolean serializeAsIndex = _isShapeWrittenUsingIndex(enumClass, format, true);
        return new EnumSerializer(v, serializeAsIndex);
    }