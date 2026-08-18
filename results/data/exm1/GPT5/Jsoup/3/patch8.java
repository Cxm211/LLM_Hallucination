boolean isValidParent(Tag child) {

        if (child.ancestors.isEmpty())
            return !child.equals(htmlTag); // unknown tags valid anywhere; html only valid at root (handled elsewhere)

        for (Tag tag : child.ancestors) {
            if (this.equals(tag))
                return true;
        }
        return false;
    }