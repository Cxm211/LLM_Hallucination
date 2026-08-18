public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times with different targets
        if (_referencedType != null) {
            if (_referencedType == ref || (_referencedType.equals(ref))) {
                return; // idempotent: already set to same reference
            }
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }