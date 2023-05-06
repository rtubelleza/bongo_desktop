public enum Board {
    PKEYBOARD,
    KEYBOARD,
    MARIMBA,
    BONGO;

    public String toFileString() {
        if (this == PKEYBOARD) {
            return "pkeyboard.png";
        } else if (this == KEYBOARD) {
            return "keyboard.png";
        } else if (this == MARIMBA) {
            return "marimba.png";
        } else {
            return "bongo.png";
        }
    }
}
