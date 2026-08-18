public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times with different target
        if (_referencedType != null && _referencedType != ref) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }