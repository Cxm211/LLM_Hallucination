public void setReference(JavaType ref)
{
    // Allow idempotent operation: re-setting to the same reference is OK
    if (_referencedType != null) {
        if (_referencedType.equals(ref)) {
            return;
        }
        throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
    }
    _referencedType = ref;
}