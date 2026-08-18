public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
    if (_valueDeserializer == deser) {
        return this;
    }
    NullValueProvider nvp = (deser == null) ? null : deser;
    return new MethodProperty(this, deser, nvp);
}