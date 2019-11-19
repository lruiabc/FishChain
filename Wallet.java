
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
 
public class Wallet {
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	//recieved money
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); 
	
	public Wallet(){
		generateKeyPair();	
	}
		
	public void generateKeyPair() {
		try {
			//generator key used secp256k1
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
			ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
	        keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
	        KeyPair keyPair = keyPairGenerator.generateKeyPair();
	        this.privateKey = keyPair.getPrivate();
	        this.publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public float getBalance() {
		float total = 0;
		UTXOs = new HashMap<String,TransactionOutput>();
		//add all of the money together. all of the UTXOs which output address is me.
        for (Map.Entry<String, TransactionOutput> item: FishChain.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.reciepienter == this.publicKey) { 
            	UTXOs.put(UTXO.id,UTXO);
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	
	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) { 
			System.out.println("#Not Enough money to send transaction. Transaction Discarded.");
			return null;
		}
		
		//create array list of inputs
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			TransactionInput curr = new TransactionInput(UTXO.id);
			inputs.add(curr);
			
			if(total > value) break;
			
		}
		
		for(TransactionInput input : inputs) {
			UTXOs.remove(input.transactionOutputId);
		}
		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		
		return newTransaction;
	}


}