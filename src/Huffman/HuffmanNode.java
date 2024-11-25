package Huffman;

class HuffmanNode {
    char data;
    int frequency;
    HuffmanNode left, right;

    HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        left = right = null;
    }
}