    void generateImpliedEndTags(String excludeTag) {
        while ((excludeTag != null && !currentElement().nodeName().equalsIgnoreCase(excludeTag)) &&
                inSorted(currentElement().nodeName().toLowerCase(), TagSearchEndTags))
            pop();
    }