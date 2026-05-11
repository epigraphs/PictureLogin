package me.Lythrilla.picturelogin.util.imgmessage;

public enum ImageChar {
    BLOCK('\u2588'),
    DARK_SHADE('\u2593'),
    MEDIUM_SHADE('\u2592'),
    LIGHT_SHADE('\u2591');

    private char c;

    private ImageChar(char c) {
        this.c = c;
    }

    public char getChar() {
        return this.c;
    }
}
