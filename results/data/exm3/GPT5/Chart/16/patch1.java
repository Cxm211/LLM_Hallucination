public void setCategoryKeys(Comparable[] categoryKeys) {
        if (categoryKeys == null) {
            throw new IllegalArgumentException("Null 'categoryKeys' argument.");
        }
        int expectedCount = 0;
        if (this.startData != null && this.startData.length > 0 && this.startData[0] != null) {
            expectedCount = this.startData[0].length;
        }
        if (categoryKeys.length != expectedCount) {
            throw new IllegalArgumentException(
                    "The number of categories does not match the data.");
        }
        for (int i = 0; i < categoryKeys.length; i++) {
            if (categoryKeys[i] == null) {
                throw new IllegalArgumentException(
                    "DefaultIntervalCategoryDataset.setCategoryKeys(): "
                    + "null category not permitted.");
            }
        }
        this.categoryKeys = categoryKeys;
        fireDatasetChanged();
    }