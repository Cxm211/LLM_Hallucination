    boolean isValidParent(Tag child) {

        if (child.ancestors.isEmpty())
            return false; // no ancestors defined, not a valid parent

        for (Tag tag : child.ancestors) {
            if (this.equals(tag))
                return true;
        }
        return false;
    }