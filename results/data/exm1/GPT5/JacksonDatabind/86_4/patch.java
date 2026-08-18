public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times
        if (_referencedType != null) {
            // If already set to the same reference, ignore duplicate call
            if (_referencedType == ref) {
                return;
            }
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }