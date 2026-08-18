protected static Boolean _isShapeWrittenUsingIndex(Class<?> enumClass,
            JsonFormat.Value format, boolean fromClass)
    {
        JsonFormat.Shape shape = (format == null) ? null : format.getShape();
        if (shape == null) {
            return null;
        }
        if (shape == Shape.ANY || shape == Shape.SCALAR) {
            return null;
        }
        if (shape == Shape.STRING) {
            return Boolean.FALSE;
        }
        if (shape == Shape.NATURAL) {
            return Boolean.TRUE;
        }
        if (shape.isNumeric() || (shape == Shape.ARRAY)) {
            return Boolean.TRUE;
        }
        throw new IllegalArgumentException(String.format(
                "Unsupported serialization shape (%s) for Enum %s, not supported as %s annotation",
                    shape, enumClass.getName(), (fromClass? "class" : "property")));
    }