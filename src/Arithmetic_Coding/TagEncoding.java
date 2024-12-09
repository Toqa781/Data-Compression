package Arithmetic_Coding;

import java.io.*;
import java.util.*;

public class TagEncoding {

    public static double encode(String sequence, Map<String, Double> probabilities, BufferedWriter writer) throws IOException {
        Map<String, double[]> ranges = new HashMap<>();
        double low = 0.0;

        // Build the ranges
        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            String symbol = entry.getKey();
            double prob = entry.getValue();
            double high = low + prob;
            ranges.put(symbol, new double[]{low, high});
            low = high;
        }

        low = 0.0;
        double high = 1.0;

        // Update the ranges while encoding
        for (char symbol : sequence.toCharArray()) {
            double rangeSize = high - low;
            high = low + rangeSize * ranges.get(String.valueOf(symbol))[1];
            low = low + rangeSize * ranges.get(String.valueOf(symbol))[0];
        }

        double tag = (low + high) / 2;

        // Output the ranges table
        writer.write(String.format("%-10s%-15s%-15s%n", "Symbol", "Lower Bound", "Upper Bound"));
        for (Map.Entry<String, double[]> entry : ranges.entrySet()) {
            writer.write(String.format("%-10s%-15.6f%-15.6f%n", entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        writer.write(String.format("Final range for the sequence: [%.6f, %.6f]%n", low, high));
        writer.write(String.format("Tag value for sequence: %.6f%n", tag));

        return tag;
    }

    public static String decode(double tag, int sequenceLength, Map<String, Double> probabilities, BufferedWriter writer) throws IOException {
        Map<String, double[]> ranges = new HashMap<>();
        double low = 0.0;

        // Build the ranges for decoding
        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            String symbol = entry.getKey();
            double prob = entry.getValue();
            double high = low + prob;
            ranges.put(symbol, new double[]{low, high});
            low = high;
        }

        StringBuilder decodedSequence = new StringBuilder();
        double currentTag = tag;

        // Decoding process
        for (int i = 0; i < sequenceLength; i++) {
            for (Map.Entry<String, double[]> entry : ranges.entrySet()) {
                double[] range = entry.getValue();
                if (currentTag >= range[0] && currentTag < range[1]) {
                    decodedSequence.append(entry.getKey());
                    double rangeSize = range[1] - range[0];
                    currentTag = (currentTag - range[0]) / rangeSize;
                    break;
                }
            }
        }

        // Output the decoded sequence
        writer.write("Decoding Process:\n");
        writer.write(String.format("Decoded sequence: %s%n", decodedSequence));

        return decodedSequence.toString();
    }

    public static Map<String, Double> parseProbabilities(List<String> alphabet, List<Double> probabilities) {
        Map<String, Double> probabilityMap = new HashMap<>();
        for (int i = 0; i < alphabet.size(); i++) {
            probabilityMap.put(alphabet.get(i), probabilities.get(i));
        }
        return probabilityMap;
    }

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("E:\\javaIntell\\Data_Compression\\src\\Arithmetic_Coding\\input.txt"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\javaIntell\\Data_Compression\\src\\Arithmetic_Coding\\output.txt"));

            int N = Integer.parseInt(reader.readLine().trim());
            List<String> alphabet = new ArrayList<>();
            List<Double> probabilities = new ArrayList<>();

            for (int i = 0; i < N; i++) {
                String symbol = reader.readLine().trim();
                double prob = Double.parseDouble(reader.readLine().trim());
                alphabet.add(symbol);
                probabilities.add(prob);
            }

            String sequence = reader.readLine().trim();
            reader.close();

            // Encode the sequence
            Map<String, Double> probabilityMap = parseProbabilities(alphabet, probabilities);
            double tag = encode(sequence, probabilityMap, writer);

            // Decode the sequence
            decode(tag, sequence.length(), probabilityMap, writer);

            writer.close();
        } catch (IOException e) {
            System.err.println("Error reading or writing to file: " + e.getMessage());
        }
    }
}
