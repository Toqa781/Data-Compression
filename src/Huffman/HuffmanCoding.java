package Huffman;

import java.io.*;
import java.util.*;

public class HuffmanCoding {

    public static void main(String[] args) {
        String inputFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\input.txt";
        String compressedFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\output.txt";
        String binaryCompressedFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\compressed.bin";
        String codesFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\huffman_codes.txt";
        String decompressedFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\decompressed.txt";

        try {
            String message = readFromFile(inputFile);

            HashMap<Character, Integer> frequencyMap = new HashMap<>();
            for (char c : message.toCharArray()) {
                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
            }
            PriorityQueue<HuffmanNode> priorityQueue =
                    new PriorityQueue<>((a, b) -> a.frequency - b.frequency);
            for (char c : frequencyMap.keySet()) {
                priorityQueue.add(new HuffmanNode(c, frequencyMap.get(c)));
            }

            while (priorityQueue.size() > 1) {
                HuffmanNode left = priorityQueue.poll();
                HuffmanNode right = priorityQueue.poll();
                HuffmanNode newNode = new HuffmanNode('$', left.frequency + right.frequency);
                newNode.left = left;
                newNode.right = right;
                priorityQueue.add(newNode);
            }

            HuffmanNode root = priorityQueue.poll();

            StringBuilder code = new StringBuilder();
            Map<Character, String> huffmanCodes = new HashMap<>();
            generateCodes(root, code, huffmanCodes);

            // Generate compressed binary stream
            StringBuilder compressedStream = new StringBuilder();
            for (char c : message.toCharArray()) {
                compressedStream.append(huffmanCodes.get(c));
            }

            // Write compressed stream and codes to files
            writeCompressedToTextFile(compressedFile, compressedStream.toString());
            writeCompressedToBinaryFile(binaryCompressedFile, compressedStream.toString());
            writeHuffmanCodesToFile(codesFile, huffmanCodes);

            System.out.println("\nCompression completed:");
            System.out.println("Compressed stream written to " + compressedFile);
            System.out.println("Compressed binary stream written to " + binaryCompressedFile);
            System.out.println("Huffman codes written to " + codesFile);

            // Decompress the compressed file
            String decompressedMessage = decompress(compressedFile, codesFile);
            writeToFile(decompressedFile, decompressedMessage);

            System.out.println("\nDecompression completed:");
            System.out.println("Decompressed file written to " + decompressedFile);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void generateCodes(HuffmanNode root, StringBuilder code, Map<Character, String> huffmanCodes) {
        if (root == null) return;
        if (root.data != '$') {
            huffmanCodes.put(root.data, code.toString());
        }
        if (root.left != null) {
            generateCodes(root.left, code.append('0'), huffmanCodes);
            code.deleteCharAt(code.length() - 1);
        }
        if (root.right != null) {
            generateCodes(root.right, code.append('1'), huffmanCodes);
            code.deleteCharAt(code.length() - 1);
        }
    }

    public static String readFromFile(String inputFile) throws IOException {
        StringBuilder message = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line);
            }
        }
        return message.toString();
    }

    public static void writeHuffmanCodesToFile(String outputFile, Map<Character, String> huffmanCodes) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }

    public static void writeCompressedToTextFile(String outputFile, String compressedStream) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(compressedStream);
        }
    }

    public static void writeCompressedToBinaryFile(String outputFile, String compressedStream) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            int byteValue = 0;
            int bitCount = 0;

            for (char bit : compressedStream.toCharArray()) {
                byteValue = (byteValue << 1) | (bit - '0');
                bitCount++;
                if (bitCount == 8) {
                    outputStream.write(byteValue);
                    byteValue = 0;
                    bitCount = 0;
                }
            }

            if (bitCount > 0) {
                byteValue <<= (8 - bitCount);
                outputStream.write(byteValue);
            }
        }
    }

    public static void writeToFile(String outputFile, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(content);
        }
    }

    public static String decompress(String compressedFile, String codesFile) throws IOException {
        String compressedStream = readFromFile(compressedFile);

        Map<String, Character> reversedHuffmanCodes = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(codesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    char character = parts[0].charAt(0);
                    String code = parts[1];
                    reversedHuffmanCodes.put(code, character);
                }
            }
        }

        StringBuilder currentCode = new StringBuilder();
        StringBuilder decompressedMessage = new StringBuilder();

        for (char bit : compressedStream.toCharArray()) {
            currentCode.append(bit);
            if (reversedHuffmanCodes.containsKey(currentCode.toString())) {
                decompressedMessage.append(reversedHuffmanCodes.get(currentCode.toString()));
                currentCode.setLength(0);
            }
        }

        return decompressedMessage.toString();
    }
}
