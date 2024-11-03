import java.util.ArrayList;
import java.util.List;

public class lz77 {
    static class Tag {
        int position;
        int length;
        char nextSymbol;

        Tag(int position, int length, char nextSymbol) {
            this.position = position;
            this.length = length;
            this.nextSymbol = nextSymbol;
        }

        public String toString() {
            return "<" + position + "," + length + "," + nextSymbol + ">";
        }
    }

    public static List<Tag> compress(String input, int searchBufferSize, int lookAheadBufferSize) {
        List<Tag> tags = new ArrayList<>();
        int currentPos = 0;

        while (currentPos < input.length()) {
            int searchStart = Math.max(0, currentPos - searchBufferSize);
            int maxMatchLength = 0;
            int matchPosition = 0;
            for (int i = currentPos - 1; i >= searchStart; i--) {
                int length = 0;
                while (length < lookAheadBufferSize &&
                        currentPos + length < input.length() &&
                        input.charAt(i + length) == input.charAt(currentPos + length)) {
                    length++;
                }
                if (length > maxMatchLength) {
                    maxMatchLength = length;
                    matchPosition = currentPos - i;
                }
            }
            char nextChar = currentPos + maxMatchLength < input.length() ? input.charAt(currentPos + maxMatchLength) : '\0';
            tags.add(new Tag(matchPosition, maxMatchLength, nextChar));
            currentPos += maxMatchLength + 1;
        }

        return tags;
    }
    public static String decompress(List<Tag> tags){
        StringBuilder decompressed=new StringBuilder();
        for(Tag tag:tags){
            int start=decompressed.length()-tag.position;
            for(int i=0;i<tag.length;i++){
                decompressed.append(decompressed.charAt(start+i));
            }
            if(tag.nextSymbol!='\0'){
                decompressed.append(tag.nextSymbol);
            }
        }
        return  decompressed.toString();
    }

    public static void main(String[] args) {
        String input = "ABAABABAABBBBBBBBBBBBA";
        System.out.println("Original Input: " + input);
        List<Tag> tags = compress(input, 12, 11);
        System.out.println("Compressed Output: " + tags);
        String decompressed = decompress(tags);
        System.out.println("Decompressed Output: " + decompressed);
    }
}