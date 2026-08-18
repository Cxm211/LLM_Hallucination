public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property != null) {
            JsonFormat.Value format = findFormatOverrides(serializers,
                    property, handledType());
            if (format != null) {
                Class<?> type = property.getType().getRawClass();
                Boolean serializeAsIndex = _isShapeWrittenUsingIndex(type,
                        format, false, property.getFeatures());
                if (serializeAsIndex != _serializeAsIndex) {
                    return new EnumSerializer(_values, serializeAsIndex);
                }
            }
        }
        return this;
    }