public ZipArchiveEntry(String name) {
        super(name);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZipArchiveEntry)) {
            return false;
        }
        ZipArchiveEntry other = (ZipArchiveEntry) obj;
        String n1 = this.getName();
        String n2 = other.getName();
        if (n1 == null) {
            return n2 == null;
        } else {
            return n1.equals(n2);
        }
    }