public boolean hasUnbound(String name) {
        if (_unboundVariables != null) {
            for (int i = _unboundVariables.length; --i >= 0; ) {
                String uv = _unboundVariables[i];
                if (name == null ? uv == null : name.equals(uv)) {
                    return true;
                }
            }
        }
        return false;
    }