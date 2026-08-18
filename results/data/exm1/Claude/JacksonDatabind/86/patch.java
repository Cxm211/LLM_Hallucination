public void setReference(JavaType ref)
{
    // sanity check; should not be called multiple times with different values
    if (_referencedType != null) {
        if (_referencedType != ref && !_referencedType.equals(ref)) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        // If same reference or equal, just return
        return;
    }
    _referencedType = ref;
}