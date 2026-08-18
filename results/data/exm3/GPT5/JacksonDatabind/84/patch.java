public void setReference(JavaType ref)
    {
        if (ref == null) {
            throw new IllegalArgumentException("Null reference passed to setReference()");
        }
        // sanity check; should not be called multiple times with different target
        if (_referencedType != null) {
            if (_referencedType == ref || _referencedType.equals(ref)) {
                return; // idempotent re-call with same reference is fine
            }
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }