public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times unless idempotent
        if (_referencedType != null) {
            // Allow idempotent re-setting with the same reference
            if (_referencedType != ref) {
                throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
            }
            return;
        }
        _referencedType = ref;
    }