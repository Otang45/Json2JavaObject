package otang.json.to.java.library;

import otang.json.to.java.library.util.Utils;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImportTypes {

    private final Map<String,String> mImportedTypes = new LinkedHashMap<>();

    public ImportTypes(){
    }
    
    public ImportTypes add(final String importType) {
        if (!Utils.isTrimEmpty(importType)) {
            mImportedTypes.put(importType, importType);   
        }
        return this;   
    }

    public ImportTypes add(final String... types) {
        for (final String type : types) {
            add(type);
        }
        return this;
    }

    public ImportTypes add(final Collection<String> types) {
        for (final String type : types) {
            add(type);
        }
        return this;
    }

    public Collection<String> values() {
        return mImportedTypes.values();   
    }

}
