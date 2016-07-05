Huffman Encoding

by Christina Harford

Overall Impression

This assignment was challenging for me. After reading through the online hand out, I figured that the project 
would be relatively trivial to implement because I understood the concept almost immediately. However, the details 
of the implementation proved to be much more tricky than I anticipated and I spent many hours debugging my 
frequency list, priority queue, and huffman tree. Overall, I enjoyed the assignment, but it was definitely harder 
than I anticipated. I believe I spent about 18 hours on it. 

Empirical Analysis

I ran HuffMark on all the files provided within Calgary, and got a 44.8% compression rate. I then altered the Huffmark class to accept all files, including already compressed ones. After that, the compression rate went down to 21.8%. 

	1. Which compresses more, binary files or text files?

Text files compress more. Most test files are written in a human readable language, and consequently follow a 
more predictable character distribution. Thus, the encoding is more efficient and compresses more. 

	2. Can you gain additional compression by double-compressing an already compressed file? If so, is there 	eventually a limit to when this no longer saves space on ordinary files? What if you built a file that was intentionally designed to compress a lot...when would it be no longer worthwhile to recompress?

It depends on the case. Some already-compressed files actually lose compression when they are double-compressed, 
and the ones that do gain compression only gain a fraction of the percentage that they gained the first 
time around. Thus, there is a limit. For most of the files I tested, the first round of compression resulted 
in a significant decrease in file size (approximately 45%), but on the second round, it compressed further by at 
most a few percent, and in some cases we lost some compression. Yes, I do think that it if a file was intentionally 
designed to compress a lot, it be worthwhile to recompress. I designed an experiment to test this by creating a file of one million bits with mostly a's and a few other characters. As I guessed, the file compression was very significant (about 80%) because the distribution for the character frequencies was very narrow and resulted in an efficient encoding. I kept compressing the file, until I reached about 35,000 bits, However, at this point, the 
compression rates began to plateau and then started going into the negatives. This is because I wrote a constant 
sized frequency table to the header of each file I compressed. When the rest of the file contained a large number 
of bits, this table was insignificant in comparison and thus I was able to compress by a higher rate. However, once 
the size of the file reaches the size of the . Therefore, the table encoded in bits is the constant limiting 
factor for every file. 

Comparison to Other Approaches

I looked up a few other compression algorithms online to see how they compared to the Huffman algorithm. A few 
algorithms that were commonly discussed were the Arithmetic algorithm, the Lempel-Ziv algorithm, and the Adaptive Huffman algorithm. The Arithmetic encoding requires more running time and resources than the Huffman encoding. However,  the overall compression ratios for the Huffman encoding are lower than those of the Arithmetic. I also compared the Huffman algorithm to the Lempel-Ziv algorithm. While both algorithms are lossless forms of compression, the former compresses from a fixed length to a variable number of bits, while the latter compresses from a variable number of bits to a fixed number. Moreover, the Huffman algorithm outperforms the Lempel-Ziv encoding when the character distribution is known beforehand, but in other cases the Lempel-Ziv results in a better compression. Finally, I compared the Huffman algorithm to an Adaptive Huffman algorithm, in which the distribution does not need to be known beforehand for it to compress efficiently because it builds the huffman tree dynamically. 


Sources for comparison: 

http://arxiv.org/pdf/1109.0216.pdf
http://www.data-compression.com/lossless.html
https://www.cs.duke.edu/csed/curious/compression/adaptivehuff.html
