package Huffman;

import java.io.*;
import java.util.*;


public class HuffmanCoding {
    public static void main(String[] args) {

        String inputFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\input.txt";
        String outputFile = "E:\\javaIntell\\Data_Compression\\src\\Huffman\\output.txt";

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

            // Build the Huffman Tree
            while (priorityQueue.size() > 1) {
                // Remove the two nodes with the lowest frequency
                HuffmanNode left = priorityQueue.poll();
                HuffmanNode right = priorityQueue.poll();

                // Create a new internal node with these two nodes
                // as children and add it back to the queue
                HuffmanNode newNode =
                        new HuffmanNode('$', left.frequency + right.frequency);

                newNode.left = left;
                newNode.right = right;
                priorityQueue.add(newNode);
            }

            // The remaining node is the root of the Huffman Tree
            HuffmanNode root = priorityQueue.poll();

            // Generate and save Huffman codes to the output file
            StringBuilder code = new StringBuilder();
            Map<Character, String> huffmanCodes = new HashMap<>();
            generateCodes(root, code, huffmanCodes);
            writeToFile(outputFile, huffmanCodes);

            System.out.println("Huffman codes written to " + outputFile);
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
            generateCodes(root.left, code.append('1'), huffmanCodes);
            code.deleteCharAt(code.length() - 1);
        }
        if (root.right != null) {
            generateCodes(root.right, code.append('0'), huffmanCodes);
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
    public static void writeToFile(String outputFile, Map<Character, String> huffmanCodes) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }
}