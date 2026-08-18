    public boolean hasUnbound(String name) {
        if (_unboundVariables != null) {
            for (int i = _unboundVariables.length; --i >= 0; ) {
                if (name.equals(((TypeVariable<?>)_unboundVariables[i]).getName())) {
                    return true;
                }
            }
        }
        return false;
    }