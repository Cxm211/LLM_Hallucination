protected static Boolean _isShapeWrittenUsingIndex(Class<?> enumClass,
            JsonFormat.Value format, boolean fromClass)
    {
        JsonFormat.Shape shape = (format == null) ? null : format.getShape();
        if (shape == null) {
            return null;
        }
        // i.e. "default", check dynamically
        if (shape == Shape.ANY || shape == Shape.SCALAR) {
            return null;
        }
        // 19-May-2016, tatu: also consider "natural" shape
        if (shape == Shape.STRING || shape == Shape.NATURAL) {
            return Boolean.FALSE;
        }
        // 01-Oct-2014, tatu: For convenience, consider "as-array" to also mean 'yes, use index')
        if (shape.isNumeric() || (shape == Shape.ARRAY)) {
            return Boolean.TRUE;
        }
        // OBJECT shape should be allowed for property annotation (means use name/string)
        if (shape == Shape.OBJECT) {
            if (fromClass) {
                throw new IllegalArgumentException(String.format(
                    "Unsupported serialization shape (%s) for Enum %s, not supported as %s annotation",
                        shape, enumClass.getName(), "class"));
            }
            return Boolean.FALSE;
        }
        // For any other unsupported shape, throw exception
        throw new IllegalArgumentException(String.format(
                "Unsupported serialization shape (%s) for Enum %s, not supported as %s annotation",
                    shape, enumClass.getName(), (fromClass? "class" : "property")));
    }