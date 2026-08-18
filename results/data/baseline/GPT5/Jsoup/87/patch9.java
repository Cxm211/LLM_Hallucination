void generateImpliedEndTags(String excludeTag) {
        while (inSorted(currentElement().nodeName(), TagSearchEndTags) && (excludeTag == null || !currentElement().nodeName().equals(excludeTag)))
            pop();
    }