package fr.hokib.hdrawer.manager.drawer.type;

import fr.hokib.hdrawer.util.location.DisplayAttributes;

public enum DrawerType {

    SOLO(new DisplayAttributes(0, 0.05f, 0.6f)),
    DUO(new DisplayAttributes(-0.225, 0, 0.4f), new DisplayAttributes(0.225, 0, 0.4f)),
    TRIO(new DisplayAttributes(0, 0.25, 0.35f),
            new DisplayAttributes(-0.25, -0.20, 0.35f),
            new DisplayAttributes(0.25, -0.20, 0.35f)),
    QUADRIO(new DisplayAttributes(-0.225, 0.25, 0.30f), new DisplayAttributes(0.225, 0.25, 0.30f),
            new DisplayAttributes(-0.225, -0.2, 0.30f), new DisplayAttributes(0.225, -0.2, 0.30f));


    private final DisplayAttributes[] attributes;

    DrawerType(DisplayAttributes... attributes) {
        this.attributes = attributes;
    }

    public static DrawerType from(final int amount) {
        return values()[amount - 1];
    }

    public DisplayAttributes[] getAttributes() {
        return this.attributes;
    }

}
