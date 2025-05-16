package PA1;




import java.io.FileInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Class Huffman that provides Huffman compression encoding and decoding of files
 * @author Lucia Moura
 *
 */
@SuppressWarnings("unchecked")
public class Huffman {

	/**
	 * 
	 * Inner class Huffman Node to Store a node of Huffman Tree
	 *
	 */
	private class HuffmanTreeNode { 
	    private int character;      // character being represented by this node (applicable to leaves)
	    private int count;          // frequency for the subtree rooted at node
	    private HuffmanTreeNode left;  // left/0  subtree (NULL if empty)
	    private HuffmanTreeNode right; // right/1 subtree subtree (NULL if empty)
	    
	    public HuffmanTreeNode(int c, int ct, HuffmanTreeNode leftNode, HuffmanTreeNode rightNode) {
	    	character = c;
	    	count = ct;
	    	left = leftNode;
	    	right = rightNode;
	    }
	    public int getChar() { return character;}
	    public Integer getCount() { return count; }
	    public HuffmanTreeNode getLeft() { return left;}
	    public HuffmanTreeNode getRight() { return right;}
		public boolean isLeaf() { return left==null ; } // since huffman tree is full; if leaf=null so must be right
	}
	
	/**
	 * 
	 * Auxiliary class to write bits to an OutputStream
	 * Since files output one byte at a time, a buffer is used to group each output of 8-bits
	 * Method close should be invoked to flush half filed buckets by padding extra 0's
	 */
	private class OutBitStream {
		OutputStream out;
		int buffer;
		int buffCount;
		
		public OutBitStream(OutputStream output) { // associates this to an OutputStream
			out = output;
			buffer=0;
			buffCount=0;
		}
		public void writeBit(int i) throws IOException { // write one bit to Output Stream (using byte buffer)
		    buffer=buffer<<1;
		    buffer=buffer+i;
		    buffCount++;
		    if (buffCount==8) { 
		    	out.write(buffer); 
		    	//System.out.println("buffer="+buffer);
		    	buffCount=0;
		    	buffer=0;
		    }
		}
		
		public void close() throws IOException { // close output file, flushing half filled byte
			if (buffCount>0) { //flush the remaining bits by padding 0's
				buffer=buffer<<(8-buffCount);
				out.write(buffer);
			}
			out.close();
		}
		
 	}
	
	/**
	 * 
	 * Auxiliary class to read bits from a file
	 * Since we must read one byte at a time, a buffer is used to group each input of 8-bits
	 * 
	 */
	private class InBitStream {
		InputStream in;
		int buffer;    // stores a byte read from input stream
		int buffCount; // number of bits already read from buffer
		public InBitStream(InputStream input) { // associates this to an input stream
			in = input;
			buffer=0; 
			buffCount=8;
		}
		public int readBit() throws IOException { // read one bit to Output Stream (using byte buffer)
			if (buffCount==8) { // current buffer has already been read must bring next byte
				buffCount=0;
				buffer=in.read(); // read next byte
				if (buffer==-1) return -1; // indicates stream ended
			}
			int aux=128>>buffCount; // shifts 1000000 buffcount times so aux has a 1 is in position of bit to read
			//System.out.println("aux="+aux+"buffer="+buffer);
			buffCount++;
			if ((aux&buffer)>0) return 1; // this checks whether bit buffcount of buffer is 1
			else return 0;
			
		}

	}
	
	/**
	 * Builds a frequency table indicating the frequency of each character/byte in the input stream
	 * @param input is a file where to get the frequency of each character/byte
	 * @return freqTable a frequency table must be an ArrayList<Integer? such that freqTable.get(i) = number of times character i appears in file 
	 *                   and such that freqTable.get(256) = 1 (adding special character representing"end-of-file")
	 * @throws IOException indicating errors reading input stream
	 */
	private ArrayList<Integer> buildFrequencyTable(InputStream input) throws IOException{
		ArrayList<Integer> freqTable= new ArrayList<Integer>(257); // declare frequency table
		for (int i=0; i<257;i++) freqTable.add(i,0); // initialize frequency values with 0

		/************ your code comes here ************/
		
		int byteRead;
		while ((byteRead = input.read()) != -1) {
		    freqTable.set(byteRead, freqTable.get(byteRead) + 1);
		}
	    
	    freqTable.set(256, 1);
		
		return freqTable;
	}

	/**
	 * Create Huffman tree using the given frequency table; the method requires a heap priority queue to run in O(nlogn) where n is the characters with nonzero frequency
	 * @param freqTable the frequency table for characters 0..255 plus 256 = "end-of-file" with same specs are return value of buildFrequencyTable
	 * @return root of the Huffman tree build by this method
	 */
	private HuffmanTreeNode buildEncodingTree(ArrayList<Integer> freqTable) {
		
		// creates new huffman tree using a priority queue based on the frequency at the root
		
		/************ your code comes here ************/
		
		PriorityQueue<HuffmanTreeNode> priorityQueue = new PriorityQueue<>(
				Comparator.comparingInt(HuffmanTreeNode::getCount)
		);
		
		for (int i =0; i < freqTable.size(); i++) {
			int frequency = freqTable.get(i);
			if (frequency > 0) {
				HuffmanTreeNode node = new HuffmanTreeNode(i, frequency, null, null);
				priorityQueue.add(node);
			}
		}
		
		while (priorityQueue.size() >1) {
			HuffmanTreeNode leftChild = priorityQueue.poll();
			HuffmanTreeNode rightChild = priorityQueue.poll();
			
			int addFrequency = leftChild.getCount() + rightChild.getCount();
			HuffmanTreeNode parent = new HuffmanTreeNode(-1, addFrequency, leftChild, rightChild);
			
			priorityQueue.add(parent);
		}
	
	    return priorityQueue.poll();
	}
	
	
	/**
	 * 
	 * @param encodingTreeRoot - input parameter storing the root of the HUffman tree
	 * @return an ArrayList<String> of length 257 where code.get(i) returns a String of 0-1 correspoding to each character in a Huffman tree
	 *                                                  code.get(i) returns null if i is not a leaf of the Huffman tree
	 */
	private ArrayList<String> buildEncodingTable(HuffmanTreeNode encodingTreeRoot) {
		ArrayList<String> code= new ArrayList<String>(257); 
		for (int i=0;i<257;i++) code.add(i,null);
		
		/************ your code comes here ************/ 
	    buildEncodingPath(encodingTreeRoot, "", code);
	    
	    return code;
	}
	private void buildEncodingPath(HuffmanTreeNode node, String path, ArrayList<String> code) {
	    if (node.isLeaf()) {
	        code.set(node.getChar(), path);
	    } else {
	        if (node.getLeft() != null) buildEncodingPath(node.getLeft(), path + "0", code);
	        if (node.getRight() != null) buildEncodingPath(node.getRight(), path + "1", code);
	    }
	}
	
	/**
	 * Encodes an input using encoding Table that stores the Huffman code for each character
	 * @param input - input parameter, a file to be encoded using Huffman encoding
	 * @param encodingTable - input parameter, a table containing the Huffman code for each character
	 * @param output - output paramter - file where the encoded bits will be written to.
	 * @throws IOException indicates I/O errors for input/output streams
	 */
	private void encodeData(InputStream input, ArrayList<String> encodingTable, OutputStream output) throws IOException {
		OutBitStream bitStream = new OutBitStream(output); // uses bitStream to output bit by bit
	   
		/************ your code comes here ************/
		
		int bytesRead;
	    while ((bytesRead = input.read()) != -1) {
	        String encodedBits = encodingTable.get(bytesRead);
	        
	        for (char bit : encodedBits.toCharArray()) {
	        	bitStream.writeBit(bit - '0');
	        }
	    }
	    
	    String endOfBits = encodingTable.get(256);
	    for (char bit : endOfBits.toCharArray()) {
	    	bitStream.writeBit(bit - '0');
	    }
	    
	    bitStream.close();
	}
	
	/**
	 * Decodes an encoded input using encoding tree, writing decoded file to output
	 * @param input  input parameter a stream where header has already been read from
	 * @param encodingTreeRoot input parameter contains the root of the Huffman tree
	 * @param output output parameter where the decoded bytes will be written to 
	 * @throws IOException indicates I/O errors for input/output streams
	 */
	private void decodeData(ObjectInputStream input, HuffmanTreeNode encodingTreeRoot, FileOutputStream output) throws IOException {
		
		InBitStream inputBitStream= new InBitStream(input); // associates a bit stream to read bits from file
		HuffmanTreeNode currentNode = encodingTreeRoot;
		/************ your code comes here ************/
		
		int bit;
	    while ((bit = inputBitStream.readBit()) != -1) {
	    	if (bit == 0) {
	    	    currentNode = currentNode.getLeft();
	    	} else {
	    	    currentNode = currentNode.getRight();
	    	}

	        if (currentNode.isLeaf()) {
	            int character = currentNode.getChar();
	            if (character == 256) {
	                break;
	            }

	            output.write(character);

	            currentNode = encodingTreeRoot;
	        }
	    }
	    
	    output.close();
	
	}
	
	public void encode(String inputFileName, String outputFileName) throws IOException {
        System.out.println("\nEncoding " + inputFileName + " " + outputFileName);

        File inputFile = new File(inputFileName);
        File outputFile = new File(outputFileName);

        
        FileInputStream input = new FileInputStream(inputFile);
        FileInputStream copyInput = new FileInputStream(inputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        ObjectOutputStream codedOutput = new ObjectOutputStream(out);
       
        ArrayList<Integer> freqTable = buildFrequencyTable(input);
        System.out.println("FrequencyTable is=" + freqTable);

        HuffmanTreeNode root = buildEncodingTree(freqTable);

        ArrayList<String> codes = buildEncodingTable(root);
        System.out.println("EncodingTable is=" + codes);

        codedOutput.writeObject(freqTable);

        encodeData(copyInput, codes, codedOutput);
       
        

        System.out.println("Number of bytes in input : " + inputFile.length());
        System.out.println("Number of bytes in output: " + outputFile.length());
   
    }

    /**
     * Method that implements Huffman decoding on encoded input into a plain output
     * @param inputFileName  - this is a file encoded (compressed) via the encode algorithm of this class 
     * @param outputFileName - this is the output where we must write the decoded file (should be original encoded file)
     * @throws IOException indicates problems with input/output streams
     * @throws ClassNotFoundException handles case where the file does not contain correct object at header
     */
    public void decode(String inputFileName, String outputFileName) throws IOException, ClassNotFoundException {
        System.out.println("\nDecoding " + inputFileName + " " + outputFileName);

        File inputFile = new File(inputFileName);
        File outputFile = new File(outputFileName);

        FileInputStream in = new FileInputStream(inputFile);
        ObjectInputStream codedInput = new ObjectInputStream(in);
        FileOutputStream output = new FileOutputStream(outputFile);
    
        ArrayList<Integer> freqTable = (ArrayList<Integer>) codedInput.readObject();
        System.out.println("FrequencyTable is=" + freqTable);

        HuffmanTreeNode root = buildEncodingTree(freqTable);

        decodeData(codedInput, root, output);

        System.out.println("Number of bytes in input : " + inputFile.length());
        System.out.println("Number of bytes in output: " + outputFile.length());

        
    }


}
	
    