public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        Boolean serializeAsIndex = _serializeAsIndex;
        if (property != null) {
            JsonFormat.Value format = findFormatOverrides(serializers,
                    property, handledType());
            if (format != null) {
                Boolean serAsIndex2 = _isShapeWrittenUsingIndex(property.getType().getRawClass(),
                        format, false);
                if (serAsIndex2 != serializeAsIndex) {
                    serializeAsIndex = serAsIndex2;
                }
            }
        }
        EnumValues v;
        SerializationConfig config = serializers.getConfig();
        Class<?> enumClass = handledType();
        if (config != null && config.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
            v = EnumValues.constructFromToString(config, (Class<Enum<?>>) enumClass);
        } else {
            v = EnumValues.constructFromName(config, (Class<Enum<?>>) enumClass);
        }
        if (serializeAsIndex != _serializeAsIndex || v != _values) {
            return new EnumSerializer(v, serializeAsIndex);
        }
        return this;
    }