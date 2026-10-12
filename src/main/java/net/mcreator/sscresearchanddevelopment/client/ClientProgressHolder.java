package net.mcreator.sscresearchanddevelopment.client;

public class ClientProgressHolder {
    private static int currentFrame = 0;

    public static void setFrame(int frame) {
        currentFrame = frame;
    }

    public static int getFrame() {
        return currentFrame;
    }
}