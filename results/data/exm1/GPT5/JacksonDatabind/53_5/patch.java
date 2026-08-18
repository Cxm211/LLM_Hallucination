public boolean hasUnbound(String name) {
        if (name == null) {
            return false;
        }
        if (_unboundVariables != null) {
            for (int i = _unboundVariables.length; --i >= 0; ) {
                if (name.equals(_unboundVariables[i])) {
                    return true;
                }
            }
        }
        return false;
    }