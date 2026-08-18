public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
    if (_valueDeserializer == deser) {
        return this;
    }
    NullValueProvider nvp = (deser == null) ? null : deser;
    return new FieldProperty(this, deser, nvp);
}