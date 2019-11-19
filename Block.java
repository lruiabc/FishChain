import java.util.ArrayList;
import java.util.Date;


public class Block{
	//hash for this block
	public String hash;
	//hash for problock
	public String previousHash; 
	
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public long timeStamp; 
	//add nonce for change hash
	public int nonce;
	
	//Create a new Block
	public Block(String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash(); 
	}
	
	//Calculate hash 
	public String calculateHash() {
		String result = StringToShaCode.doSha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return result;
	}
	
	//Mining
	public void mineBlock(int difficulty) {
		merkleRoot = StringToShaCode.getMerkleRoot(transactions);
		String target = StringToShaCode.getDificultyString(difficulty); 
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
	}
	
	//Add transactions to this block
	public boolean addTransaction(Transaction transaction) {
		//edge case
		if(transaction == null) return false;		
		if(previousHash.equals("0")) {
			
		}else{
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
			
		}
		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
	
}

