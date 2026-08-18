public void setReference(JavaType ref)
{
    // sanity check; should not be called multiple times with different values
    if (_referencedType != null) {
        if (_referencedType.equals(ref)) {
            return;
        }
        throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
    }
    _referencedType = ref;
}