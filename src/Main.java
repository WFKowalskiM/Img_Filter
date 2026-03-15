import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class Main {
    private static BufferedImage image;

    private static final int[][] sobelX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
    };
    private static final int[][] sobelY = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
    };

    public static void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                image = ImageIO.read(file);
                System.out.println("Image loaded successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static BufferedImage getImage() {
        return image;
    }
    public static void setImage(BufferedImage image) {
        Main.image = image;
    }

    public static void applySmoothingFilter() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        int[][] kernel = {
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };
        int kernelSize = 3;
        int kernelSum = 9;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int rSum = 0, gSum = 0, bSum = 0;

                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        int pixel = image.getRGB(x + kx - 1, y + ky - 1);
                        Color color = new Color(pixel);

                        rSum += color.getRed() * kernel[ky][kx];
                        gSum += color.getGreen() * kernel[ky][kx];
                        bSum += color.getBlue() * kernel[ky][kx];
                    }
                }
                int r = Math.min(255, rSum / kernelSum);
                int g = Math.min(255, gSum / kernelSum);
                int b = Math.min(255, bSum / kernelSum);
                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        setImage(output);
    }
    public static void applyMedianFilter() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] rValues = new int[9];
                int[] gValues = new int[9];
                int[] bValues = new int[9];
                int index = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = image.getRGB(x + kx, y + ky);
                        Color color = new Color(pixel);

                        rValues[index] = color.getRed();
                        gValues[index] = color.getGreen();
                        bValues[index] = color.getBlue();
                        index++;
                    }
                }

                Arrays.sort(rValues);
                Arrays.sort(gValues);
                Arrays.sort(bValues);
                int r = rValues[4];
                int g = gValues[4];
                int b = bValues[4];

                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        setImage(output);
    }

    public static void applySobelFilter() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gxR = 0, gxG = 0, gxB = 0;
                int gyR = 0, gyG = 0, gyB = 0;

                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int pixel = image.getRGB(x + kx - 1, y + ky - 1);
                        Color color = new Color(pixel);

                        gxR += sobelX[ky][kx] * color.getRed();
                        gxG += sobelX[ky][kx] * color.getGreen();
                        gxB += sobelX[ky][kx] * color.getBlue();

                        gyR += sobelY[ky][kx] * color.getRed();
                        gyG += sobelY[ky][kx] * color.getGreen();
                        gyB += sobelY[ky][kx] * color.getBlue();
                    }
                }

                int r = (int) Math.min(255, Math.sqrt(gxR * gxR + gyR * gyR));
                int g = (int) Math.min(255, Math.sqrt(gxG * gxG + gyG * gyG));
                int b = (int) Math.min(255, Math.sqrt(gxB * gxB + gyB * gyB));

                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        setImage(output);
    }

    public static void applyThreshold(int threshold) { // do dylatacji i erozji
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                Color color = new Color(pixel);

                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                int binaryValue = (gray >= threshold) ? 255 : 0;
                output.setRGB(x, y, new Color(binaryValue, binaryValue, binaryValue).getRGB());
            }
        }
        setImage(output);
    }

    public static int[][] createStructuringElement(int width, int height) {
        int[][] structuringElement = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                structuringElement[y][x] = 1;
            }
        }

        return structuringElement;
    }


    public static void applyDilationFilter(int[][] structuringElement) {
        int width = Main.image.getWidth();
        int height = image.getHeight();
        int elementWidth = structuringElement[0].length;
        int elementHeight = structuringElement.length;
        int offsetX = elementWidth / 2;
        int offsetY = elementHeight / 2;

        BufferedImage output = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean dilate = false;

                for (int ey = 0; ey < elementHeight; ey++) {
                    for (int ex = 0; ex < elementWidth; ex++) {
                        int nx = x + ex - offsetX;
                        int ny = y + ey - offsetY;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            int pixel = image.getRGB(nx, ny) & 0xFF; // Get grayscale value
                            if (pixel == 255 && structuringElement[ey][ex] == 1) {
                                dilate = true;
                                break;
                            }
                        }
                    }
                    if (dilate) break;
                }

                output.setRGB(x, y, dilate ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
        setImage(output);
    }

    public static void applyErosionFilter(int[][] structuringElement) {
        BufferedImage input = getImage();
        int width = input.getWidth();
        int height = input.getHeight();
        int elementWidth = structuringElement[0].length;
        int elementHeight = structuringElement.length;
        int offsetX = elementWidth / 2;
        int offsetY = elementHeight / 2;

        BufferedImage output = new BufferedImage(width, height, input.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean erode = true;

                for (int ey = 0; ey < elementHeight; ey++) {
                    for (int ex = 0; ex < elementWidth; ex++) {
                        int nx = x + ex - offsetX;
                        int ny = y + ey - offsetY;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            int pixel = input.getRGB(nx, ny) & 0xFF;
                            if (pixel == 0 && structuringElement[ey][ex] == 1) {
                                erode = false;
                                break;
                            }
                        } else if (structuringElement[ey][ex] == 1) {
                            erode = false;
                            break;
                        }
                    }
                    if (!erode) break;
                }

                output.setRGB(x, y, erode ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
        setImage(output);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Image Processing GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JLabel imageLabel = new JLabel("Load an image to get started", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(600, 400));
        frame.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5));

        JButton loadButton = new JButton("Load Image");
        JButton smoothButton = new JButton("Smoothing");
        JButton medianButton = new JButton("Median");
        JButton sobelButton = new JButton("Sobel");
        JButton dilateButton = new JButton("Dilation");
        JButton erodeButton = new JButton("Erosion");

        buttonPanel.add(loadButton);
        buttonPanel.add(smoothButton);
        buttonPanel.add(medianButton);
        buttonPanel.add(sobelButton);
        buttonPanel.add(dilateButton);
        buttonPanel.add(erodeButton);

        JLabel widthLabel = new JLabel("Width:");
        JTextField widthField = new JTextField("3", 5); // Default value: 3
        JLabel heightLabel = new JLabel("Height:");
        JTextField heightField = new JTextField("3", 5); // Default value: 3

        frame.add(buttonPanel, BorderLayout.SOUTH);

        JPanel structuringElementPanel = new JPanel();
        structuringElementPanel.setLayout(new FlowLayout());

        structuringElementPanel.add(widthLabel);
        structuringElementPanel.add(widthField);
        structuringElementPanel.add(heightLabel);
        structuringElementPanel.add(heightField);

        frame.add(structuringElementPanel, BorderLayout.NORTH);

        loadButton.addActionListener(e -> {
            loadImage();
            if (image != null) {
                imageLabel.setIcon(new ImageIcon(image));
                imageLabel.setText(null);
            }
        });

        smoothButton.addActionListener(e -> applyFilter("smooth", imageLabel));
        medianButton.addActionListener(e -> applyFilter("median", imageLabel));
        sobelButton.addActionListener(e -> applyFilter("sobel", imageLabel));
        dilateButton.addActionListener(e -> {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                if(width > 0 && height > 0) {
                    applyFilter("dilate", imageLabel, width, height);
                }
                else {
                    System.out.println("Invalid size for structuring element!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input for structuring element size!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        erodeButton.addActionListener(e -> {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                if(width > 0 && height > 0) {
                    applyFilter("erode", imageLabel, width, height);
                }
                else {
                    System.out.println("Invalid size for structuring element!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input for structuring element size!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private static void resetimage(JLabel imageLabel) {
        if (image != null) {
            imageLabel.setIcon(new ImageIcon(image));
            if(imageLabel.getText() != null) {
                imageLabel.setText(null);
            }
        }
    }

    private static void applyFilter(String filterType, JLabel imageLabel, int... param) {
        int param1 = param.length > 0 ? param[0] : 3;
        int param2 = param.length > 1 ? param[1] : 3;
        if (image == null) {
            JOptionPane.showMessageDialog(null, "Please load an image first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        switch (filterType) {
            case "smooth":
                applySmoothingFilter();
                resetimage(imageLabel);
                break;
            case "median":
                applyMedianFilter();
                resetimage(imageLabel);
                break;
            case "sobel":
                applySobelFilter();
                resetimage(imageLabel);
                break;
            case "dilate":
                applyThreshold(128);
                applyDilationFilter(createStructuringElement(param1, param2));
                resetimage(imageLabel);
                break;
            case "erode":
                applyThreshold(128);
                applyErosionFilter(createStructuringElement(param1, param2));
                resetimage(imageLabel);
                break;
        }
    }

    public static void main(String[] args) {
        createAndShowGUI();
    }
}
