    boolean isValidParent(Tag child) {
        if (child.ancestors.isEmpty())
            return false; // HTML tag has empty ancestors, but no Tag can be its parent

        for (Tag tag : child.ancestors) {
            if (this.equals(tag))
                return true;
        }
        return false;
    }