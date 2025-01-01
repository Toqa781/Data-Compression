package Vector_Quantization;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class VectorQuantizationDecompression {
    public static void main(String[] args) throws Exception {
        //load the compressed data
        String compressedFile = "E:\\javaIntell\\Data_Compression\\src\\Vector_Quantization\\compressed.txt";
        BufferedReader reader = new BufferedReader(new FileReader(compressedFile));

        //read the overhead information (metadata)
        String line = reader.readLine();  //image width
        int width = Integer.parseInt(line.split(":")[1].trim());

        line = reader.readLine();  //image height
        int height = Integer.parseInt(line.split(":")[1].trim());

        line = reader.readLine();  //block size
        int blockSize = Integer.parseInt(line.split(":")[1].trim());

        line = reader.readLine();  //codebook size
        int codebookSize = Integer.parseInt(line.split(":")[1].trim());

        //skip "Compressed Indices" line
        reader.readLine();

        // Read the compressed indices
        List<Integer> compressedIndices = new ArrayList<>();
        line = reader.readLine();
        String[] indices = line.trim().split(" ");
        for (String index : indices) {
            compressedIndices.add(Integer.parseInt(index));
        }

        //skip "Codebook" line
        reader.readLine();

        //read the codebook
        List<int[]> codebook = new ArrayList<>();
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            String[] values = line.trim().split(" ");
            int[] block = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                block[i] = Integer.parseInt(values[i]);
            }
            codebook.add(block);
        }

        reader.close();

        //reconstruct the image from the compressed data
        BufferedImage decompressedImage = decompressImage(compressedIndices, codebook, blockSize, width, height);

        //save the decompressed image as JPG
        saveImage(decompressedImage, "E:\\javaIntell\\Data_Compression\\src\\Vector_Quantization\\decompressed.jpg");
    }

    //reconstruct the image from the compressed data
    public static BufferedImage decompressImage(List<Integer> compressedIndices, List<int[]> codebook, int blockSize, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int blockIndex = 0;

        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                if (blockIndex >= compressedIndices.size()) {
                    break;
                }

                int index = compressedIndices.get(blockIndex++); //points to a specific vector in the codebook
                int[] block = codebook.get(index); // use the index to get the corresponding codebook

                int i = 0;
                for (int j = y; j < y + blockSize && j < height; j++) {
                    for (int k = x; k < x + blockSize && k < width; k++) {
                        int pixelValue = Math.min(255, Math.max(0, block[i++]));
                        //set the pixel value directly for images
                        image.getRaster().setSample(k, j, 0, pixelValue);
                    }
                }
            }
        }
        return image;
    }

    // Save the decompressed image to file as JPEG
    public static void saveImage(BufferedImage image, String filename) throws IOException {
        javax.imageio.ImageIO.write(image, "JPEG", new File(filename));
    }
}
