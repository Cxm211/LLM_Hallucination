public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
    if (_valueDeserializer == deser) {
        return this;
    }
    return new CreatorProperty(this, deser, _nullProvider);
}