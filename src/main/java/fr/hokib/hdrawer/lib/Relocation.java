package fr.hokib.hdrawer.lib;

import static fr.hokib.hdrawer.lib.LibraryLoader.alias;

public record Relocation(String[] from, String[] to) {

    public static Relocation of() {
        return new Relocation(new String[0], new String[0]);
    }

    public static Relocation of(String from, String to) {
        return new Relocation(new String[]{alias(from)}, new String[]{alias(to)});
    }

    public static Relocation of(String from1, String to1, String from2, String to2) {
        return new Relocation(new String[]{alias(from1), alias(from2)}, new String[]{alias(to1), alias(to2)});
    }

    public static Relocation of(String from1, String to1, String from2, String to2, String from3, String to3) {
        return new Relocation(new String[]{alias(from1), alias(from2), alias(from3)}, new String[]{alias(to1), alias(to2), alias(to3)});
    }

    public static Relocation of(String from1, String to1, String from2, String to2, String from3, String to3, String from4, String to4) {
        return new Relocation(new String[]{alias(from1), alias(from2), alias(from3), alias(from4)}, new String[]{alias(to1), alias(to2), alias(to3), alias(to4)});
    }

    public int size() {
        return from.length;
    }
}