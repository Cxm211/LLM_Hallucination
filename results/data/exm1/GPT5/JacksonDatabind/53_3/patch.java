public boolean hasUnbound(String name) {
        if (_unboundVariables != null) {
            for (int i = _unboundVariables.length; --i >= 0; ) {
                if (java.util.Objects.equals(name, _unboundVariables[i])) {
                    return true;
                }
            }
        }
        return false;
    }