package lzw;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class lzw {
    private String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    private void writeFile(String content, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(content);
        }
    }
    private void writeCompressedFile(List<Integer> compressed, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (int code : compressed) {
                bw.write(code + " ");
            }
        }
    }
    private List<Integer> readCompressedFile(String filePath) throws IOException {
        List<Integer> compressed = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                String[] codes = line.split(" ");
                for (String code : codes) {
                    compressed.add(Integer.parseInt(code));
                }
            }
        }
        return compressed;
    }
    public static List<Integer> compress(String input) {
        int dictSize = 128;
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < dictSize; i++) {
            dictionary.put("" + (char) i, i);
        }
        String current = "";
        List<Integer> result = new ArrayList<>();
        for (char c : input.toCharArray()) {
            String currentPlusChar = current + c;
            if (dictionary.containsKey(currentPlusChar)) {
                current = currentPlusChar;
            } else {
                result.add(dictionary.get(current));
                dictionary.put(currentPlusChar, dictSize++);
                current = "" + c;
            }
        }
        if (!current.equals("")) {
            result.add(dictionary.get(current));
        }
        return result;
    }
    public static String decompress(List<Integer> compressed) {
        int dictSize = 128;
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < dictSize; i++) {
            dictionary.put(i, "" + (char) i);
        }
        String current = "" + (char) (int) compressed.remove(0);
        StringBuilder result = new StringBuilder(current);
        for (int code : compressed) {
            String entry;
            if (dictionary.containsKey(code)) {
                entry = dictionary.get(code);
            } else if (code == dictSize) {
                entry = current + current.charAt(0);
            } else {
                throw new IllegalArgumentException("Invalid compressed code: " + code);
            }
            result.append(entry);
            dictionary.put(dictSize++, current + entry.charAt(0));
            current = entry;
        }
        return result.toString();
    }
    public static void main(String[] args) {
        lzw lzw = new lzw();
        String inputFilePath = "E:\\javaIntell\\Data_Compression\\src\\lzw\\input.txt";
        String compressedFilePath = "E:\\javaIntell\\Data_Compression\\src\\lzw\\compressed.tx";
        String decompressedFilePath = "E:\\javaIntell\\Data_Compression\\src\\lzw\\decompressed.txt";
        try {
            String input = lzw.readFile(inputFilePath);
            System.out.println("Original string from file: " + input);
            List<Integer> compressed = compress(input);
            lzw.writeCompressedFile(compressed, compressedFilePath);
            System.out.println("Compressed data written to file: " + compressedFilePath);
            List<Integer> compressedData = lzw.readCompressedFile(compressedFilePath);
            String decompressed = decompress(compressedData);
            lzw.writeFile(decompressed, decompressedFilePath);
            System.out.println("Decompressed string written to file: " + decompressedFilePath);
            if (input.equals(decompressed)) {
                System.out.println("Success! The decompressed string matches the original.");
            } else {
                System.out.println("Error: The decompressed string does not match the original.");
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
