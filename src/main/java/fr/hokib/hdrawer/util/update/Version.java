package fr.hokib.hdrawer.util.update;

public class Version implements Comparable<Version> {
    private final String[] parts;

    public Version(final String version) {
        this.parts = version.split("\\.");
    }

    @Override
    public int compareTo(final Version other) {
        final int minLength = Math.min(this.parts.length, other.parts.length);

        for (int i = 0; i < minLength; i++) {
            final int thisPart = Integer.parseInt(this.parts[i]);
            final int otherPart = Integer.parseInt(other.parts[i]);

            if (thisPart < otherPart) {
                return -1;
            } else if (thisPart > otherPart) {
                return 1;
            }
        }


        return Integer.compare(parts.length, other.parts.length);
    }
}