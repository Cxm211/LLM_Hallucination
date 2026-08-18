public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
    if (_valueDeserializer == deser) {
        return this;
    }
    return new ObjectIdReferenceProperty(this, deser, _nullProvider);
}