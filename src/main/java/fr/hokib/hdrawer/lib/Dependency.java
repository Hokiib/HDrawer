package fr.hokib.hdrawer.lib;

import java.util.HashMap;
import java.util.Map;

public class Dependency {
    private static final String MAVEN_REPOSITORY = "https://repo.maven.apache.org/maven2/";
    // Adventure
    private final String test;
    private final String path;
    private final String repository;
    private final boolean inner;
    private final Relocation relocation;

    private Map<String, String> relocationMap;

    public Dependency(final String test, final String path, final Relocation... relocations) {
        this(test, path, MAVEN_REPOSITORY, relocations);
    }

    public Dependency(final String test, final String path, final String repository, final Relocation... relocations) {
        this(test, path, repository, false, relocations);
    }

    public Dependency(final String test, final String path, final String repository, final boolean inner, final Relocation... relocations) {
        this.test = LibraryLoader.alias(test);
        this.path = LibraryLoader.alias(path);
        this.repository = repository;
        this.inner = inner;
        if (relocations.length == 1) {
            this.relocation = relocations[0];
            return;
        } else if (relocations.length == 0) {
            this.relocation = Relocation.of();
            return;
        }

        int size = 0;
        for (final Relocation relocation : relocations) {
            size = size + relocation.size();
        }
        if (size < 1) {
            this.relocation = Relocation.of();
            return;
        }

        final String[] from = new String[size];
        final String[] to = new String[size];
        for (int i = 0; i < size; i++) {
            for (final Relocation relocation : relocations) {
                for (int i1 = 0; i1 < relocation.size(); i1++) {
                    from[i] = relocation.from()[i1];
                    to[i] = relocation.to()[i1];
                    i++;
                }
            }
        }
        this.relocation = new Relocation(from, to);
    }

    public Map<String, String> getRelocationMap() {
        if (this.relocationMap == null) {
            final Map<String, String> map = new HashMap<>();
            for (int i = 0; i < this.relocation.size(); i++) {
                final String key = LibraryLoader.alias(this.relocation.from()[i]);
                final String value = LibraryLoader.alias(this.relocation.to()[i]);
                map.put(key, value);
            }
            this.relocationMap = map;
        }
        return this.relocationMap;
    }

    public String getTest() {
        return this.test;
    }

    public String getPath() {
        return this.path;
    }

    public String getRepository() {
        return this.repository;
    }

    public boolean isInner() {
        return this.inner;
    }

    public Relocation getRelocation() {
        return this.relocation;
    }
}
