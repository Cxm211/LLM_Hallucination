public void setReference(JavaType ref)
    {
        if (_referencedType != null) {
            if (_referencedType == ref) {
                return; // allow idempotent re-setting with same reference
            }
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }