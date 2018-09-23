import javax.swing.*;
import java.awt.*;

public class main {
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("JBeauty");
        mainFrame.setContentPane(new FaceBeautify().mainPanel);
        mainFrame.getContentPane().setSize(800,800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
