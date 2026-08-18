public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times with different reference
        if (_referencedType != null) {
            if (_referencedType != ref) {
                throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
            }
            // if same reference, no-op
            return;
        }
        _referencedType = ref;
    }