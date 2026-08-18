public boolean hasUnbound(String name) {
        if (_unboundVariables != null && name != null) {
            for (int i = _unboundVariables.length; --i >= 0; ) {
                if (name.equals(_unboundVariables[i])) {
                    return true;
                }
            }
        }
        return false;
    }