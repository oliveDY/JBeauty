import java.awt.*;
import java.awt.image.BufferedImage;



public class MyCommon {

    public static BufferedImage applyFilter(BufferedImage inputImage, double[][] filter){
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < inputImage.getHeight(); i++){
            for (int j = 0; j < inputImage.getWidth(); j++){
                double r= 0, g = 0, b = 0;
                for(int m = i - 1; m <= i + 1; m++){
                    for(int n = j -1; n <= j+1; n++){
                        if(m < 0 || m >= inputImage.getHeight() || n < 0 || n >= inputImage.getWidth()){
                            continue;
                        }
                        else{
                            r += filter[m-(i-1)][n-(j-1)] * new Color(inputImage.getRGB(n, m)).getRed();
                            g += filter[m-(i-1)][n-(j-1)] * new Color(inputImage.getRGB(n, m)).getGreen();
                            b += filter[m-(i-1)][n-(j-1)] * new Color(inputImage.getRGB(n, m)).getBlue();
                        }
                    }
                }
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                Color c = new Color((int)r, (int)g, (int)b);
                outputImage.setRGB(j, i, c.getRGB());
            }
        }

        return outputImage;

    }
}
