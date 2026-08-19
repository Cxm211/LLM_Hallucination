// ===== FIXED org.jfree.data.category.DefaultIntervalCategoryDataset :: DefaultIntervalCategoryDataset [lines 90-93] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-16-fixed/source/org/jfree/data/category/DefaultIntervalCategoryDataset.java =====
    public DefaultIntervalCategoryDataset(double[][] starts, double[][] ends) {
        this(DataUtilities.createNumberArray2D(starts),
                DataUtilities.createNumberArray2D(ends));
    }

// ===== FIXED org.jfree.data.category.DefaultIntervalCategoryDataset :: setCategoryKeys(Comparable[]) [lines 334-351] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-16-fixed/source/org/jfree/data/category/DefaultIntervalCategoryDataset.java =====
    public void setCategoryKeys(Comparable[] categoryKeys) {
        if (categoryKeys == null) {
            throw new IllegalArgumentException("Null 'categoryKeys' argument.");
        }
        if (categoryKeys.length != getCategoryCount()) {
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
