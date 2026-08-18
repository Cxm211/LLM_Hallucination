public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property != null) {
            JsonFormat.Value format = findFormatOverrides(serializers,
                    property, handledType());
            if (format != null) {
                Boolean serializeAsIndex = _isShapeWrittenUsingIndex(property.getType().getRawClass(),
                        format, false);
                if (serializeAsIndex != _serializeAsIndex) {
                    JsonFormat.Shape shape = format.getShape();
                    if (shape == Shape.STRING || shape == Shape.NATURAL) {
                        return new EnumSerializer(
                            EnumValues.constructFromToString(serializers.getConfig(),
                                (Class<Enum<?>>) property.getType().getRawClass()),
                            serializeAsIndex);
                    } else {
                        return new EnumSerializer(_values, serializeAsIndex);
                    }
                }
            }
        }
        return this;
    }