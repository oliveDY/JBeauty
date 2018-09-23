import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Point {
    public int x;
    public int y;
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

public class FaceBeautifyBackend {


    public static  int KERNEL_SIZE = 3;
    private final static String API_KEY = "kmDzTCyai5IOa6Mk97RSFbf5pKPh0c82";
    private final static String API_SECRET = "25zuvL8rRD1G-4PLrGQ5hD_JeBWnoym6";
    private final static String API_URL = "https://api-us.faceplusplus.com/facepp/v3/detect";

    public static BufferedImage beautify(File raw_img) {
        System.out.print("Start beautify");

        // get base64 encoded;
        String encoedImg = encodeImgFileBase64(raw_img);

        // get face recognition result
        try {
            JSONObject result = apiCall(encoedImg);
            System.out.println(result.toString());
            return processImage(raw_img, result);
        } catch (URISyntaxException e) {
            System.out.println("URI syntax: " + e.getMessage());
        }

        return null;
    }


    private static BufferedImage processImage(File raw_img_file, JSONObject detect_info) {
        BufferedImage raw_image = null;

        // extract information
        List<Point> faceBoundary = getFaceBoundaryPoints(detect_info);
        List<Point> leftEyeBoundary = getEyeBoundaryPoints(detect_info, "left");
        List<Point> rightEyeBoundary = getEyeBoundaryPoints(detect_info, "right");
        List<Point> mouthBoundary = getMouthBoundaryPoints(detect_info);
        List<Point> leftEyebrowBoundary = getEyebrowBoundaryPoints(detect_info, "left");
        List<Point> rightEyebrowBoundary = getEyebrowBoundaryPoints(detect_info, "right");

        try {
            raw_image = ImageIO.read(raw_img_file);
        } catch (Exception e) {
            System.out.println("Read image failed.");
        }
        return smoothSkin(raw_image, faceBoundary, leftEyeBoundary, rightEyeBoundary,
                mouthBoundary, leftEyebrowBoundary, rightEyebrowBoundary);
    }

    ////////////////////////////////////////////
    // smooth skin
    ////////////////////////////////////////////

    private static int[] neighborProcess(BufferedImage raw_img, int x, int y, int kernel_size) {
        int offset = kernel_size / 2;
        int r =0, g=0, b= 0;
        for (int m = x - offset; m <= x + offset; m++) {
            for (int n = y - offset; n <= y + offset; n++) {
                if (m < 0 || m >= raw_img.getWidth() || n < 0 || n >= raw_img.getHeight())
                    continue;
                r += new Color(raw_img.getRGB(m, n)).getRed();
                g += new Color(raw_img.getRGB(m, n)).getGreen();
                b += new Color(raw_img.getRGB(m, n)).getBlue();
            }
        }
        r /= (kernel_size * kernel_size);
        g /= (kernel_size * kernel_size);
        b /= (kernel_size * kernel_size);
        int[] result =  {r,g,b};
        return result;
    }

    private static BufferedImage smoothSkin(BufferedImage raw_img, List<Point> faceBoundary,
                                            List<Point> leftEyeBoundary, List<Point> rightEyeBoundary,
                                            List<Point> mouthBoundary, List<Point> leftEyebrowBoundary,List<Point> rightEyebrowBoundary) {
        BufferedImage newImg = new BufferedImage(raw_img.getWidth(),raw_img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < raw_img.getWidth(); x++) {
            for (int y = 0; y < raw_img.getHeight(); y++) {
                if (!insideBounary(x, y, leftEyeBoundary) && !insideBounary(x, y, rightEyeBoundary)
                        && !insideBounary(x, y, mouthBoundary) && !insideBounary(x, y, leftEyebrowBoundary)
                        && !insideBounary(x, y, rightEyebrowBoundary)
                        && insideBounary(x, y, faceBoundary)) {
//                    int r =0, g=0, b= 0;
//                    int offset = KERNEL_SIZE / 2;
//                    for (int m = x - offset; m <= x + offset; m++) {
//                        for (int n = y - offset; n <= y + offset; n++) {
//                            if (m < 0 || m >= raw_img.getWidth() || n < 0 || n >= raw_img.getHeight())
//                                continue;
//                            r += new Color(raw_img.getRGB(m, n)).getRed();
//                            g += new Color(raw_img.getRGB(m, n)).getGreen();
//                            b += new Color(raw_img.getRGB(m, n)).getBlue();
//                        }
//                    }
//                    Color c = null;
//                    r /= (KERNEL_SIZE * KERNEL_SIZE);
//                    g /= (KERNEL_SIZE * KERNEL_SIZE);
//                    b /= (KERNEL_SIZE * KERNEL_SIZE);
                    Color c = null;
                    try {
                        int[] rgb = neighborProcess(raw_img, x, y, KERNEL_SIZE);
                        c = new Color(rgb[0], rgb[1], rgb[2]);
                    } catch (Exception e) {
                        int[] rgb = neighborProcess(raw_img, x, y, 3);// default kernel size= 3 when out of boundary
                        c = new Color(rgb[0], rgb[1], rgb[2]);
                    }
                    newImg.setRGB(x, y, c.getRGB());
                } else {
                    newImg.setRGB(x , y, raw_img.getRGB(x, y));
                }
            }
        }

        return newImg;
    }


    ////////////////////////////////////////////
    // Boundary check
    ////////////////////////////////////////////

    private static Point getPoint(JSONObject landmark, String key) {
        int x = landmark.getJSONObject(key).getInt("x");
        int y = landmark.getJSONObject(key).getInt("y");
        return new Point(x, y);
    }

    /**
     * Get face bounary points
     * @param detect_info
     * @return
     */
    private static List<Point> getFaceBoundaryPoints(JSONObject detect_info) {
        List<Point> face_boundary = new ArrayList<Point>();
        JSONObject face_1 = detect_info.getJSONArray("faces").getJSONObject(0);
        JSONObject landmarks = face_1.getJSONObject("landmark");

        // chins
        face_boundary.add(getPoint(landmarks, "contour_chin"));

        // left counter
        for (int i = 1; i <= 16; i++) {
            face_boundary.add(getPoint(landmarks, "contour_left" + i));
        }

        // right counter
        for (int i = 1; i <= 16; i++) {
            face_boundary.add(getPoint(landmarks, "contour_right" + i));
        }

        // left eyebrow up
        face_boundary.add(getPoint(landmarks, "left_eyebrow_left_corner"));
        face_boundary.add(getPoint(landmarks, "left_eyebrow_upper_left_quarter"));
        face_boundary.add(getPoint(landmarks, "left_eyebrow_upper_middle"));
        face_boundary.add(getPoint(landmarks, "left_eyebrow_upper_right_quarter"));
        face_boundary.add(getPoint(landmarks, "left_eyebrow_upper_right_corner"));

        // right eyebrow up
        face_boundary.add(getPoint(landmarks, "right_eyebrow_upper_left_corner"));
        face_boundary.add(getPoint(landmarks, "right_eyebrow_upper_left_quarter"));
        face_boundary.add(getPoint(landmarks, "right_eyebrow_upper_middle"));
        face_boundary.add(getPoint(landmarks, "right_eyebrow_upper_right_quarter"));
        face_boundary.add(getPoint(landmarks, "right_eyebrow_right_corner"));

        return face_boundary;
    }


    /**
     * Get Eye boundary points
     * @param detect_info
     * @param pos
     * @return
     */
    private static List<Point> getEyeBoundaryPoints(JSONObject detect_info, String pos) {
        List<Point> boundary = new ArrayList<Point>();
        JSONObject face_1 = detect_info.getJSONArray("faces").getJSONObject(0);
        JSONObject landmarks = face_1.getJSONObject("landmark");

        boundary.add(getPoint(landmarks, pos + "_eye_left_corner"));
        boundary.add(getPoint(landmarks, pos + "_eye_upper_left_quarter"));
        boundary.add(getPoint(landmarks, pos + "_eye_top"));
        boundary.add(getPoint(landmarks, pos + "_eye_upper_right_quarter"));
        boundary.add(getPoint(landmarks, pos + "_eye_right_corner"));
        boundary.add(getPoint(landmarks, pos + "_eye_lower_right_quarter"));
        boundary.add(getPoint(landmarks, pos + "_eye_bottom"));
        boundary.add(getPoint(landmarks, pos + "_eye_lower_left_quarter"));

        return boundary;
    }

    /**
     * Get Eyebow boundary points
     * @param detect_info
     * @param pos
     * @return
     */
    private static List<Point> getEyebrowBoundaryPoints(JSONObject detect_info, String pos) {
        List<Point> boundary = new ArrayList<Point>();
        JSONObject face_1 = detect_info.getJSONArray("faces").getJSONObject(0);
        JSONObject landmarks = face_1.getJSONObject("landmark");

        if (pos == "left") {
            boundary.add(getPoint(landmarks, "left_eyebrow_left_corner"));
            boundary.add(getPoint(landmarks, "left_eyebrow_upper_left_quarter"));
            boundary.add(getPoint(landmarks, "left_eyebrow_upper_middle"));
            boundary.add(getPoint(landmarks, "left_eyebrow_upper_right_corner"));
            boundary.add(getPoint(landmarks, "left_eyebrow_upper_right_quarter"));
            boundary.add(getPoint(landmarks, "left_eyebrow_lower_middle"));
            boundary.add(getPoint(landmarks, "left_eyebrow_lower_right_quarter"));
            boundary.add(getPoint(landmarks, "left_eyebrow_lower_right_corner"));
        } else {
            boundary.add(getPoint(landmarks, "right_eyebrow_upper_left_corner"));
            boundary.add(getPoint(landmarks, "right_eyebrow_upper_left_quarter"));
            boundary.add(getPoint(landmarks, "right_eyebrow_upper_middle"));
            boundary.add(getPoint(landmarks, "right_eyebrow_upper_right_quarter"));
            boundary.add(getPoint(landmarks, "right_eyebrow_right_corner"));
            boundary.add(getPoint(landmarks, "right_eyebrow_lower_left_corner"));
            boundary.add(getPoint(landmarks, "right_eyebrow_lower_left_quarter"));
            boundary.add(getPoint(landmarks, "right_eyebrow_lower_middle"));
            boundary.add(getPoint(landmarks, "right_eyebrow_lower_right_quarter"));
        }

        return boundary;
    }

    /**
     * Get mouth boundary points
     * @param detect_info
     * @return
     */
    private static List<Point>  getMouthBoundaryPoints(JSONObject detect_info) {
        List<Point> boundary = new ArrayList<Point>();
        JSONObject face_1 = detect_info.getJSONArray("faces").getJSONObject(0);
        JSONObject landmarks = face_1.getJSONObject("landmark");

        boundary.add(getPoint(landmarks,  "mouth_left_corner"));
        boundary.add(getPoint(landmarks,  "mouth_right_corner"));
        boundary.add(getPoint(landmarks,  "mouth_upper_lip_top"));
        boundary.add(getPoint(landmarks,  "mouth_upper_lip_bottom"));
        boundary.add(getPoint(landmarks,  "mouth_lower_lip_top"));
        boundary.add(getPoint(landmarks,  "mouth_lower_lip_bottom"));

        String[] positions = {"left", "right"};
        for (String pos: positions) {
            for (int i = 1; i <=4;i ++) {
                boundary.add(getPoint(landmarks, "mouth_upper_lip_" + pos + "_contour" + i));
            }

            for (int i = 1; i <=3;i ++) {
                boundary.add(getPoint(landmarks, "mouth_lower_lip_" + pos + "_contour" + i));
            }
        }

        return boundary;
    }
    /**
     * Check if (x,y)inside a boundary
     * Ref: https://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon/2922778#2922778
     * @param x
     * @param y
     * @return
     */
    private static boolean insideBounary(int x, int y, List<Point> boundary) {
        boolean c = false;
        for (int i=0, j=boundary.size()-1; i<boundary.size(); j=i++) {
            if ( (boundary.get(i).y > y) != (boundary.get(j).y > y) &&
                    (x < (boundary.get(j).x - boundary.get(i).x) * (y - boundary.get(i).y) / (boundary.get(j).y - boundary.get(i).y) + boundary.get(i).x )) {
                c = !c;
            }
        }
        return c;
    }



    ////////////////////////////////////////////
    // API calling
    ////////////////////////////////////////////

    private static String encodeImgFileBase64(File file) {
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.getEncoder().encode(bytes), StandardCharsets.US_ASCII);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedfile;
    }

    private static JSONObject apiCall(String imgBase64) throws URISyntaxException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(API_URL);

        // parameter
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("api_key", API_KEY));
        urlParameters.add(new BasicNameValuePair("api_secret", API_SECRET));
        urlParameters.add(new BasicNameValuePair("image_base64", imgBase64));
        urlParameters.add(new BasicNameValuePair("return_landmark", "2"));
        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported encode: " + e.getMessage());
        }

        // REQUEST
        try {
            HttpResponse response = httpclient.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer r = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                r.append(line);
            }
            System.out.println(r);
            return new JSONObject(r.toString());
        } catch (IOException e) {
            System.out.println("IO exception: " + e.getMessage());
            return null;
        }
    }
}
