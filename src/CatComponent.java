public enum CatComponent {
    HEAD,
    MOUTH,
    LEFT_PAW,
    RIGHT_PAW;

    public String toFileString() {
        if (this == HEAD) {
            return "head.png";
        } else if (this == MOUTH) {
            return "mouth.png";
        } else if (this == LEFT_PAW) {
            return "left_paw.png";
        } else {
            return "right_paw.png";
        }
    }
}
