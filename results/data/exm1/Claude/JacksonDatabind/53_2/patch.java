public boolean hasUnbound(String name) {
    if (_unboundVariables != null) {
        for (int i = 0; i < _unboundVariables.length; i++) {
            if (name.equals(_unboundVariables[i])) {
                return true;
            }
        }
    }
    return false;
}