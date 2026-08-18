boolean isValidParent(Tag child) {

        if (child.ancestors.isEmpty())
            return true;

        for (Tag tag : child.ancestors) {
            if (this.equals(tag))
                return true;
        }
        return false;
    }