public boolean hasUnbound(String name) {
        if (_unboundVariables != null) {
            for (int i = _unboundVariables.length; --i >= 0; ) {
                final String var = _unboundVariables[i];
                if (name == null) {
                    if (var == null) {
                        return true;
                    }
                } else if (name.equals(var)) {
                    return true;
                }
            }
        }
        return false;
    }