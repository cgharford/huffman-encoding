import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {
    
	// GUI for application
    private HuffViewer myViewer;
    
    // Huffman maps created when compressing and decompressing
    private HashMap<Integer, String> compressMap = new HashMap<Integer, String>();
    private HashMap<String, Integer> decompressMap = new HashMap<String, Integer>();
    private TreeNode root;
    
    // Frequency counts for each character in the file
    int[] characterCounts;
    
    // Keep track of bits in file before and after compression 
    private  int uncompressedBitsTotal = 0;
    private int compressedBitsTotal = 0;

    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
    	BitInputStream input = new BitInputStream(in);
    	BitOutputStream output = new BitOutputStream(out);
    	int charSize = IHuffConstants.BITS_PER_WORD;
    	compressedBitsTotal = 0;
    	
    	// Write magic number to the header
    	output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
    	compressedBitsTotal += BITS_PER_INT;
    	
    	// Write the character frequency table to the header
    	for(int k=0; k < ALPH_SIZE; k++){
            output.writeBits(BITS_PER_INT, characterCounts[k]);
            compressedBitsTotal += BITS_PER_INT;
        }
    	
    	// Read in the first character
        int character = input.readBits(charSize);
        String encodedString;
    	
        // Keep reading from file until you've reached the end
    	while(character != -1) {
        	// Look up character in the compression hashmap to get encoded value
        	encodedString = compressMap.get(character);
        	
        	// Write bits one at a time to get the correct encoded value
        	for (int i = 0; i < encodedString.length(); i++){
                output.writeBits(1, Character.getNumericValue(encodedString.charAt(i)));
                compressedBitsTotal++;
            }
        	// Read in next word
        	character = input.readBits(charSize);
        }
    	
    	// Write EOF string to the output stream
    	String eofString = compressMap.get(PSEUDO_EOF);
    	for (int i = 0; i < eofString.length(); i++){
            output.writeBits(1, Character.getNumericValue(eofString.charAt(i)));
            compressedBitsTotal++;
        }
    	
    	// If the compressed bits total is greater than the uncompressed bits total, alert the user and 
    	// only continue if the user has selected the option "Force Compression"
    	if (compressedBitsTotal > uncompressedBitsTotal) {
    		if (force) {
    			myViewer.update("Forcing compression");
    		}
    		else {
    			myViewer.showError("More compressed bits than uncompressed; 'Force Compression' if you want to compress this file.");
    			return 0;
    		}
    	}
    	
    	// Close input and output streams
    	input.close();
    	output.close();
    	
    	myViewer.update("Total compressed bits: " + compressedBitsTotal);
    	myViewer.showMessage("Compressed bits: " + compressedBitsTotal);
        return compressedBitsTotal;
    }

    public int preprocessCompress(InputStream in) throws IOException { 
    	int charSize = IHuffConstants.BITS_PER_WORD;
        BitInputStream input = new BitInputStream(in);
        characterCounts = new int[ALPH_SIZE];
        uncompressedBitsTotal = 0;
        
        // Read in first character
        int character = input.readBits(charSize);
        
        // Keep reading from file until you've reached the end
        while(character != -1) {
        	// Update frequency of character in list and read in next character
        	characterCounts[character]++;
        	character = input.readBits(charSize);
        	uncompressedBitsTotal += 8;
        }
       
        // Build the huffman tree and hashmap from the frequency array
        buildMap(characterCounts, false);
        
        // Close the input stream
        input.close();
        
        myViewer.update("Total uncompressed bits: " + uncompressedBitsTotal);
        return uncompressedBitsTotal;
    }
    
    public int uncompress(InputStream in, OutputStream out) throws IOException {
    	int[] counts = new int[IHuffConstants.ALPH_SIZE];
    	BitInputStream input = new BitInputStream(in);
    	BitOutputStream output = new BitOutputStream(out);
    	int charSize = IHuffConstants.BITS_PER_WORD;
    	int decompressedBits = 0;
    	
    	// If magic number in the header is not the same, 
    	int magic = input.readBits(BITS_PER_INT);
        if (magic != MAGIC_NUMBER){
        	myViewer.update("Magic number does not match...aborting decompression");
        }
        
        // Read the frequency table from the header
        for(int k=0; k < ALPH_SIZE; k++){
            int bits = input.readBits(BITS_PER_INT);
            counts[k] = bits;
        }
        
        // Rebuild the mappings from the header information
        buildMap(counts, true);
       
    	int character;
    	String encodedString = "";
    	int value = 0;
    	
    	// Until we've reached the EOF
        while (value != PSEUDO_EOF) {
        	character = input.readBits(1);
        	encodedString += ((Integer)character).toString();
        	// Check to see if we've hit a mapped encoding one bit at a time
        	if (decompressMap.containsKey(encodedString)) {
        		value = decompressMap.get(encodedString);
        		output.write(value);
        		// Reset the string to start over on the next 'character'
        		encodedString = "";
        		decompressedBits++;
        	}     	
        }
        
        // Close input and output streams
        input.close();
    	output.close();
    	
    	myViewer.showMessage("Compression complete.");
        return decompressedBits;
    }
    
    private void showString(String s){
        myViewer.update(s);
    }
    
    public void setViewer(HuffViewer viewer) {
        myViewer = viewer;
    }
    
    public void buildMap(int[] counts, boolean reverse) {
    	// Create a priority queue to order all of the frequencies
    	PriorityQueue<TreeNode> priorityQueue = new PriorityQueue<TreeNode>();
    	
    	// For each character with a valid frequency, add it to the priority queue
    	for (int i = 0; i < counts.length; i ++) {
    		if (counts[i] == 0) {
    			continue;
    		}
    		else {
    			// Create a node with the frequency value and insert it into the priority queue
    			TreeNode node = new TreeNode(i, counts[i]);
    			priorityQueue.add(node);
    		}
    	}    	
    	
    	// Add the eof  
    	priorityQueue.add(new TreeNode(PSEUDO_EOF, 1));
    	
    	TreeNode leftChild; 
    	TreeNode rightChild;
    	
    	// Build huffman tree until priority queue is empty
    	while (priorityQueue.size() > 1) {
    		leftChild = priorityQueue.remove();
    		rightChild = priorityQueue.remove();
    		priorityQueue.add(new TreeNode(0, leftChild.myWeight + rightChild.myWeight, leftChild, rightChild));
		}
    	// Extract root
		root = priorityQueue.remove();
		
		// Build the map encodings recursively 
    	buildMapRecursively(root, "", reverse);
    }
    
    public void buildMapRecursively(TreeNode root, String encodedString, boolean reverse) {
    	// Base case
    	if (root == null) {
    		return;
    	}
    	// If we've reached the bottom of the huffman tree, put the encodings in the proper map
    	if (root.myLeft == null && root.myRight == null) {
    		// Reversed depending on whether or this function is being called from compress or uncompress
    		if (reverse) {
    			decompressMap.put(encodedString, root.myValue);
    		}
    		else {
    			compressMap.put(root.myValue, encodedString);
    		}
    		return;
    	}
    	// Recursively call on left and right child until reaches the base case
    	buildMapRecursively(root.myLeft, encodedString + "0", reverse);
    	buildMapRecursively(root.myRight, encodedString + "1", reverse);
    };
}

