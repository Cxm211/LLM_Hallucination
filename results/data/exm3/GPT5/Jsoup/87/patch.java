public String tagName() {
        // Return the normalized (lower-case) tag name for parser comparisons,
        // ensuring parsing logic remains case-insensitive even when preserving case is enabled.
        return tag.normalName();
    }