public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times with different targets
        if (_referencedType != null) {
            if (_referencedType != ref) {
                throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
            }
            return; // already set to the same reference; ok
        }
        _referencedType = ref;
    }