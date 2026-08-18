public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
    if (_valueDeserializer == deser) {
        return this;
    }
    NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
    return new ObjectIdReferenceProperty(this, deser, nvp);
}