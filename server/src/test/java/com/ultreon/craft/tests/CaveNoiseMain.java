package com.ultreon.craft.tests;

import com.ultreon.craft.world.gen.CaveNoiseGenerator;
import com.ultreon.libs.commons.v0.Mth;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

public class CaveNoiseMain {

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(255, 255, BufferedImage.TYPE_INT_RGB);
        CaveNoiseGenerator caveNoiseGenerator = new CaveNoiseGenerator(System.currentTimeMillis());

        for (int x = 0; x < 255; x++) {
            for (int y = 0; y < 255; y++) {
                for (int z = 0; z < 255; z++) {
                    int val = (int) (Mth.clamp(caveNoiseGenerator.evaluateNoise(x, z, y), 0, 1));
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    rgb = r + val << 16 | g + val << 8 | b + val;
                    image.setRGB(x, y, rgb);
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setVisible(true);
        });
    }
}
