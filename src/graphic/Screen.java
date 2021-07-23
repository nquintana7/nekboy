package graphic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import java.util.Random;

public class Screen extends JPanel
{
    private static final Random random = new Random();

    private BufferedImage canvas;
    GPU gpu;

    public Screen(int width, int height, GPU gpu){
        this.gpu = gpu;
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        fillCanvas();
    }

    public Dimension getPreferredSize() {
        return new Dimension(canvas.getWidth()*3, canvas.getHeight()*3);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, 0,0,160*3, 144*3,null);
    }

    public void fillCanvas() {
        for (int y = 0; y < 144; y++) {
            for (int x = 0; x < 160; x++) {
                canvas.setRGB(x, y, gpu.tileSet[x][y]);
            }
        }
        repaint();
    }
}
