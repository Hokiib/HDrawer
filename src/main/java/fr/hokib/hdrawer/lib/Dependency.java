package fr.hokib.hdrawer.lib;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Dependency {
    private static final String MAVEN_REPOSITORY = "https://repo.maven.apache.org/maven2/";
    // Adventure
    @Getter
    private final String test;
    @Getter
    private final String path;
    @Getter
    private final String repository;
    @Getter
    private final boolean inner;
    @Getter
    private final Relocation relocation;

    private Map<String, String> relocationMap;

    public Dependency(String test, String path, Relocation... relocations) {
        this(test, path, MAVEN_REPOSITORY, relocations);
    }

    public Dependency(String test, String path, String repository, Relocation... relocations) {
        this(test, path, repository, false, relocations);
    }

    public Dependency(String test, String path, boolean inner, Relocation... relocations) {
        this(test, path, MAVEN_REPOSITORY, inner, relocations);
    }

    public Dependency(String test, String path, String repository, boolean inner, Relocation... relocations) {
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
        for (Relocation relocation : relocations) {
            size = size + relocation.size();
        }
        if (size < 1) {
            this.relocation = Relocation.of();
            return;
        }

        String[] from = new String[size];
        String[] to = new String[size];
        for (int i = 0; i < size; i++) {
            for (Relocation relocation : relocations) {
                for (int i1 = 0; i1 < relocation.size(); i1++) {
                    from[i] = relocation.from[i1];
                    to[i] = relocation.to[i1];
                    i++;
                }
            }
        }
        this.relocation = new Relocation(from, to);
    }

    public Map<String, String> getRelocationMap() {
        if (relocationMap == null) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < relocation.size(); i++) {
                String key = LibraryLoader.alias(relocation.from[i]);
                String value = LibraryLoader.alias(relocation.to[i]);
                map.put(key, value);
            }
            relocationMap = map;
        }
        return relocationMap;
    }

}
