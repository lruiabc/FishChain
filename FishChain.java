import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class FishChain {
	// blockChain
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	//unblocked Transaction id 
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty = 3;
	public static float minimumTransaction = 0.001f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Wallet walletC;
	public static Transaction genesisTransaction;
 
	public static void main(String[] args) {	
		
		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		//Create wallets:
		walletA = new Wallet();
		walletB = new Wallet();	
		walletC = new Wallet();	
		
		Wallet coinbase = new Wallet();
		
		//create genesis transaction, which sends 100 NoobCoin to walletA: 
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 1000);
		genesisTransaction.generateSignature(coinbase.privateKey);	
		
		 //manually add the Transactions Output
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepienter, genesisTransaction.value, genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //A.value in the UTXOs
		
		//dummy block
		System.out.println("Creating and Mining dummy block... ");
		Block dummy = new Block("0");
		dummy.addTransaction(genesisTransaction);
		addBlock(dummy);
		
		//block1
		Block block1 = new Block(dummy.hash);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 400f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		
		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
		System.out.println("WalletB is Attempting to send funds (5) to WalletC...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20f));
		block3.addTransaction(walletB.sendFunds( walletC.publicKey, 5f));
		addBlock(block3);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		System.out.println("WalletC's balance is: " + walletC.getBalance());
		//walletB.getBalance();
		System.out.println("\nWalletB's UTXOs size: " + walletB.UTXOs.size());
		
		
		Block block4 = new Block(block3.hash);
		System.out.println("\nWalletA is Attempting to send funds (60) to WalletC...");
		block4.addTransaction(walletA.sendFunds( walletC.publicKey, 60f));
		addBlock(block4);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletC's balance is: " + walletC.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		System.out.println("totly: " + (walletA.getBalance() + walletB.getBalance() + walletC.getBalance()));
		
		isChainValid();
		
	}
	
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); 
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//Verifies hash
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//Verifies prehash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//Verifies difficulty
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepienter != currentTransaction.reciepienter) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepienter != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

}
