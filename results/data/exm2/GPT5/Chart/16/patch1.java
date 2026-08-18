public void setCategoryKeys(Comparable[] categoryKeys) {
        if (categoryKeys == null) {
            throw new IllegalArgumentException("Null 'categoryKeys' argument.");
        }
        int categoryCount = (this.startData != null && this.startData.length > 0)
                ? this.startData[0].length : 0;
        if (categoryKeys.length != categoryCount) {
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