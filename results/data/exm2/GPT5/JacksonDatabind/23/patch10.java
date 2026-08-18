public DoubleSerializer() { super(Double.class, JsonParser.NumberType.DOUBLE, "number"); }

public boolean isEmpty(SerializerProvider prov, Object value) {
    if (value == null) {
        return true;
    }
    return ((Double) value).doubleValue() == 0.0d;
}