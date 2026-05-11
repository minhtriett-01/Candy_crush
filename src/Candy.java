import java.awt.image.BufferedImage;

public class Candy {
    public int color;
    public SpecialType specialType;
    public BufferedImage image;
    public boolean activated = false; // prevents candy explosion in an endless loop, indicates whether the candy has been triggered or not

    // constructor of 1 candy

    public Candy(int color, SpecialType specialType, BufferedImage image) {
        this.color = color;
        this.specialType = specialType;
        this.image = image;
    }
}