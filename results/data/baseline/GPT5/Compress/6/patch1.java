public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ZipArchiveEntry other = (ZipArchiveEntry) obj;
        String thisName = getName();
        String otherName = other.getName();
        if (thisName == null) {
            return otherName == null;
        } else {
            return thisName.equals(otherName);
        }
    }