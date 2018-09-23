import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FaceBeautify {
    private JButton selectPortraitButton;
    public JPanel mainPanel;
    private JButton beautifyButton;
    private JLabel imgLabel;
    private JTextField kernelSizeInput;

    private BufferedImage selectedRawImage;
    private File rawImage;
    private BufferedImage beautifiedImage;

    // max displaying image
    final static int maxHeight = 400;
    final static int maxWidth = 400;


    public FaceBeautify() {
        this.init();
        this.rawImage = null;
    }

    private void init() {
        this.selectImageBtnClicked();
        this.selectBeautifyButton();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // imgBeautifyBtn
    ////////////////////////////////////////////////////////////////////////////////////

    private void selectBeautifyButton() {
        this.beautifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FaceBeautifyBackend.KERNEL_SIZE = Integer.parseInt(kernelSizeInput.getText());
                BufferedImage newImg = FaceBeautifyBackend.beautify(rawImage);
                displayImage(newImg);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // imgChoseBtn
    ////////////////////////////////////////////////////////////////////////////////////

    private void selectImageBtnClicked() {
        // image select btton
        this.selectPortraitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    rawImage = new File(file.toURI());

                    System.out.println("Opening: " + file.getName() + "." );
                    loadImage(file);
                    displayImage(selectedRawImage);
;                }
            }
        });
    }

    private void loadImage(File imgFile) {
        try {
            this.selectedRawImage = ImageIO.read(imgFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.mainPanel, "Could not load image: " + imgFile.getName());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // imageLabel
    ////////////////////////////////////////////////////////////////////////////////////


    /**
     * Display image
     */
    private void displayImage(BufferedImage image) {
        Image img = null;

        // tranform size if needed
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            double scale = 1.0;
            if (image.getWidth() > image.getHeight()) {
                scale = maxWidth * 1.0 / image.getWidth();
            } else {
                scale = maxHeight * 1.0 / image.getHeight();
            }
            AffineTransform transform = new AffineTransform();
            transform.scale(scale, scale);
            AffineTransformOp scaleOp =  new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage after = new BufferedImage((int)Math.round(image.getWidth() * scale), (int)Math.round(image.getHeight() * scale), BufferedImage.TYPE_INT_ARGB);
            image = scaleOp.filter(image, after);
        }

        try {
            File outputfile = new File("temp_raw.jpg");
            ImageIO.write(image, "jpg", outputfile);
            img = ImageIO.read(outputfile);
        } catch (IOException e) {
            // TODO: do something
        }

        ImageIcon icon = new ImageIcon(img);
        this.imgLabel.setIcon(icon);
    }
}
