public PropertyName findNameForSerialization(Annotated a)
{
    String name = null;

    JsonGetter jg = _findAnnotation(a, JsonGetter.class);
    if (jg != null) {
        name = jg.value();
    } else {
        JsonProperty pann = _findAnnotation(a, JsonProperty.class);
        if (pann != null) {
            name = pann.value();
        }
    }

    if (name != null && !name.isEmpty()) {
        return PropertyName.construct(name);
    }

    if (_hasAnnotation(a, JsonSerialize.class)
            || _hasAnnotation(a, JsonView.class)
            || _hasAnnotation(a, JsonRawValue.class)) {
        return PropertyName.USE_DEFAULT;
    }

    return null;
}