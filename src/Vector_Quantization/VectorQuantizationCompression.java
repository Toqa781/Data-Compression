package Vector_Quantization;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class VectorQuantizationCompression {
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);

        // Prompt user for block size and codebook size
        System.out.print("Enter block size: ");
        int blockSize = input.nextInt();

        System.out.print("Enter codebook size: ");
        int codebookSize = input.nextInt();

        // Input image file
        String inputImagePath = "E:\\javaIntell\\Data_Compression\\src\\Vector_Quantization\\image.jpg"; // Grayscale image
        BufferedImage image = loadImage(inputImagePath);

        //create codebook using LBG algorithm
        List<int[]> codebook = createCodebook(image, blockSize, codebookSize);

        //compress the image
        List<Integer> compressedIndices = compressImage(image, codebook, blockSize);

        //save the compressed data
        saveCompressedData("E:\\javaIntell\\Data_Compression\\src\\Vector_Quantization\\compressed.txt", compressedIndices, codebook, blockSize, codebookSize, image);

        input.close();
    }

    public static BufferedImage loadImage(String filePath) throws IOException {
        return javax.imageio.ImageIO.read(new File(filePath));
    }

    //create codebook using LBG algorithm
    public static List<int[]> createCodebook(BufferedImage image, int blockSize, int codebookSize) {
        List<int[]> blocks = extractBlocks(image, blockSize);
        List<int[]> codebook = new ArrayList<>();

        //start with the mean of all blocks
        int[] initialVector = calculateMeanVector(blocks);
        codebook.add(initialVector);

        while (codebook.size() < codebookSize) {
            //split each codebook vector into two slightly different vectors
            List<int[]> newCodebook = new ArrayList<>();
            for (int[] vector : codebook) {
                newCodebook.add(addPerturbation(vector, 1));  // Slightly increase values
                newCodebook.add(addPerturbation(vector, -1)); // Slightly decrease values
            }

            codebook = newCodebook;

            //Refine codebook using Lloyd's iteration
            boolean converged = false;
            while (!converged) {
                Map<int[], List<int[]>> clusters = new HashMap<>();
                for (int[] vector : codebook) {
                    clusters.put(vector, new ArrayList<>());
                }

                //assign each block to the nearest codebook vector
                for (int[] block : blocks) {
                    int[] nearest = findNearestCodebookVector(block, codebook);
                    clusters.get(nearest).add(block);
                }

                //update codebook vectors as the mean of their clusters
                converged = true;
                for (Map.Entry<int[], List<int[]>> entry : clusters.entrySet()) {
                    int[] oldVector = entry.getKey();
                    List<int[]> cluster = entry.getValue();

                    if (!cluster.isEmpty()) {
                        int[] newVector = calculateMeanVector(cluster);
                        if (!Arrays.equals(oldVector, newVector)) {
                            converged = false;
                            codebook.remove(oldVector);
                            codebook.add(newVector);
                        }
                    }
                }
            }
        }

        return codebook;
    }

    // Add perturbation to a vector
    private static int[] addPerturbation(int[] vector, int delta) {
        int[] perturbed = vector.clone();
        for (int i = 0; i < perturbed.length; i++) {
            perturbed[i] += delta;
        }
        return perturbed;
    }

    // Find the nearest codebook vector to a given block
    private static int[] findNearestCodebookVector(int[] block, List<int[]> codebook) {
        int minDist = Integer.MAX_VALUE;
        int[] nearest = null;

        for (int[] vector : codebook) {
            int dist = calculateDistance(block, vector);
            if (dist < minDist) {
                minDist = dist;
                nearest = vector;
            }
        }
        return nearest;
    }

    // Calculate the mean vector from a list of blocks
    private static int[] calculateMeanVector(List<int[]> blocks) {
        int[] mean = new int[blocks.get(0).length];
        for (int[] block : blocks) {
            for (int i = 0; i < block.length; i++) {
                mean[i] += block[i];
            }
        }
        for (int i = 0; i < mean.length; i++) {
            mean[i] /= blocks.size();
        }
        return mean;
    }

    // Calculate the distance between two vectors
    private static int calculateDistance(int[] block, int[] vector) {
        int dist = 0;
        for (int i = 0; i < block.length; i++) {
            dist += Math.pow(block[i] - vector[i], 2);
        }
        return dist;
    }

    // Extract blocks from the image
    public static List<int[]> extractBlocks(BufferedImage image, int blockSize) {
        List<int[]> blocks = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int[] block = new int[blockSize * blockSize];
                int index = 0;
                for (int j = y; j < y + blockSize && j < height; j++) {
                    for (int i = x; i < x + blockSize && i < width; i++) {
                        block[index++] = image.getRGB(i, j) & 0xFF;  // Grayscale value
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    // Compress the image using the codebook
    public static List<Integer> compressImage(BufferedImage image, List<int[]> codebook, int blockSize) {
        List<Integer> compressedIndices = new ArrayList<>();
        List<int[]> imageBlocks = extractBlocks(image, blockSize);

        for (int[] block : imageBlocks) {
            int bestMatchIndex = findBestMatch(block, codebook);
            compressedIndices.add(bestMatchIndex);
        }

        return compressedIndices;
    }

    // Find the closest codebook entry to a block (naive Euclidean distance)
    public static int findBestMatch(int[] block, List<int[]> codebook) {
        int minDist = Integer.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < codebook.size(); i++) {
            int[] codebookBlock = codebook.get(i);
            int dist = calculateDistance(block, codebookBlock);
            if (dist < minDist) {
                minDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    // Save the compressed data (indices and codebook) to a .txt file
    public static void saveCompressedData(String filename, List<Integer> compressedIndices, List<int[]> codebook, int blockSize, int codebookSize, BufferedImage image) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        // Write overhead information (metadata)
        writer.write("Image Width: " + image.getWidth() + "\n");
        writer.write("Image Height: " + image.getHeight() + "\n");
        writer.write("Block Size: " + blockSize + "\n");
        writer.write("Codebook Size: " + codebookSize + "\n");

        // Write the compressed matrix (compressed indices)
        writer.write("Compressed Indices:\n");
        for (Integer index : compressedIndices) {
            writer.write(index + " ");
        }
        writer.newLine();

        // Write the codebook
        writer.write("Codebook:\n");
        for (int[] block : codebook) {
            for (int value : block) {
                writer.write(value + " ");
            }
            writer.newLine();
        }

        writer.close();
    }
}
