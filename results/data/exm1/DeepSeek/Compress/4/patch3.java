public ChangeSetResults perform(ArchiveInputStream in, ArchiveOutputStream out)
            throws IOException {
        ChangeSetResults results = new ChangeSetResults();
        
        List<Change> changeList = new ArrayList<Change>(changes);
        
        // Process replaceMode adds, but skip if later deleted
        for (int i = 0; i < changeList.size(); i++) {
            Change change = changeList.get(i);
            if (change.type() == Change.TYPE_ADD && change.isReplaceMode()) {
                String name = change.getEntry().getName();
                boolean laterDeleted = false;
                for (int j = i + 1; j < changeList.size(); j++) {
                    Change later = changeList.get(j);
                    if (later.type() == Change.TYPE_DELETE && name != null && name.equals(later.targetFile())) {
                        laterDeleted = true;
                        break;
                    } else if (later.type() == Change.TYPE_DELETE_DIR && name != null) {
                        if (name.startsWith(later.targetFile() + "/")) {
                            laterDeleted = true;
                            break;
                        }
                    }
                }
                if (!laterDeleted) {
                    copyStream(change.getInput(), out, change.getEntry());
                    results.addedFromChangeSet(name);
                    changeList.remove(i);
                    i--;
                }
            }
        }
        
        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            boolean copy = true;
            String name = entry.getName();
            
            for (int i = 0; i < changeList.size(); i++) {
                Change change = changeList.get(i);
                if (change.type() == Change.TYPE_DELETE && name != null && name.equals(change.targetFile())) {
                    copy = false;
                    changeList.remove(i);
                    i--;
                    results.deleted(name);
                    break;
                } else if (change.type() == Change.TYPE_DELETE_DIR && name != null && name.startsWith(change.targetFile() + "/")) {
                    copy = false;
                    results.deleted(name);
                    break;
                }
            }
            
            if (copy) {
                if (!results.hasBeenAdded(name)) {
                    copyStream(in, out, entry);
                    results.addedFromStream(name);
                } else {
                    IOUtils.copy(in, new ByteArrayOutputStream());
                }
            } else {
                IOUtils.copy(in, new ByteArrayOutputStream());
            }
        }
        
        // Process non-replaceMode adds that haven't been added and not later deleted
        for (int i = 0; i < changeList.size(); i++) {
            Change change = changeList.get(i);
            if (change.type() == Change.TYPE_ADD && !change.isReplaceMode()) {
                String name = change.getEntry().getName();
                if (name == null) continue;
                if (results.hasBeenAdded(name)) continue;
                
                boolean laterDeleted = false;
                for (int j = i + 1; j < changeList.size(); j++) {
                    Change later = changeList.get(j);
                    if (later.type() == Change.TYPE_DELETE && name.equals(later.targetFile())) {
                        laterDeleted = true;
                        break;
                    } else if (later.type() == Change.TYPE_DELETE_DIR && name.startsWith(later.targetFile() + "/")) {
                        laterDeleted = true;
                        break;
                    }
                }
                if (!laterDeleted) {
                    copyStream(change.getInput(), out, change.getEntry());
                    results.addedFromChangeSet(name);
                    changeList.remove(i);
                    i--;
                }
            }
        }
        
        return results;
    }