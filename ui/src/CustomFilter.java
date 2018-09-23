
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


public class CustomFilter {
    private JTextField filter_value_0;
    private JTextField filter_value_2;
    private JTextField filter_value_1;
    private JTextField filter_value_3;
    private JTextField filter_value_4;
    private JTextField filter_value_5;
    private JTextField filter_value_6;
    private JTextField filter_value_7;
    private JTextField filter_value_8;
    private JButton clearBtn;
    private JButton applyBtn;
    public JPanel mainPanel;
    private JLabel imageLabel;
    private JButton imgChoseBtn;

    public BufferedImage selectedRawImage;
    public BufferedImage filteredImage;
    public double[][] filterMatrix;

    // max displaying image
    final static int maxHeight = 500;
    final static int maxWidth = 500;


    public CustomFilter() {
        this.filterMatrix = new double[3][3];
        this.init();
    }

    private void init() {
        this.selectImageBtnClicked();
        this.applyBtnClicked();
        this.clearFilterBtnClick();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // imgChoseBtn
    ////////////////////////////////////////////////////////////////////////////////////

    private void selectImageBtnClicked() {
        // image select btton
        this.imgChoseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Opening: " + file.getName() + "." );
                    loadImage(file);
                    displayImage(selectedRawImage);
                }
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
        this.imageLabel.setIcon(icon);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Filter matrix related
    ////////////////////////////////////////////////////////////////////////////////////

    private void updateFilterMatrix() {
        this.filterMatrix[0][0] = Double.parseDouble(filter_value_0.getText());
        this.filterMatrix[0][1] = Double.parseDouble(filter_value_1.getText());
        this.filterMatrix[0][2] = Double.parseDouble(filter_value_2.getText());
        this.filterMatrix[1][0] = Double.parseDouble(filter_value_3.getText());
        this.filterMatrix[1][1] = Double.parseDouble(filter_value_4.getText());
        this.filterMatrix[1][2] = Double.parseDouble(filter_value_5.getText());
        this.filterMatrix[2][0] = Double.parseDouble(filter_value_6.getText());
        this.filterMatrix[2][1] = Double.parseDouble(filter_value_7.getText());
        this.filterMatrix[2][2] = Double.parseDouble(filter_value_8.getText());
    }


    /**
     * Apply filter when apply btn clicked
     */
    private void applyBtnClicked() {
        // image select btton
        this.applyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // update filter matrix
                updateFilterMatrix();
                // apply filter
                BufferedImage result = MyCommon.applyFilter(selectedRawImage, filterMatrix);
                // show image
                displayImage(result);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Clear filter
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Clear filter effect and display original image
     */
    private void clearFilterBtnClick() {
        this.clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayImage(selectedRawImage);
            }
        });
    }

}
