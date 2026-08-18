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
                    return new EnumSerializer(_values, serializeAsIndex);
                }
                // if shape explicitly string/natural, use toString() values
                if (format.getShape() == Shape.STRING || format.getShape() == Shape.NATURAL) {
                    EnumValues newValues = EnumValues.constructFromToString(serializers.getConfig(),
                            property.getType().getRawClass());
                    if (!_values.equals(newValues)) {
                        return new EnumSerializer(newValues, serializeAsIndex);
                    }
                }
            }
        }
        return this;
    }