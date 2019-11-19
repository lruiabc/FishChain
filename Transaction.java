import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
	public String transactionId; // this is also the hash of the transaction.
	public PublicKey sender; // sender address/public key.
	public PublicKey reciepienter; // Recipienter address/public key.
	public float value;
	public byte[] signature; // this is to prevent anybody else from spending funds in our wallet.
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	// Constructor: 
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepienter = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	public Transaction(PublicKey from, PublicKey to, float value) {
		this.sender = from;
		this.reciepienter = to;
		this.value = value;
	}
	
	public boolean processTransaction() {
		
		
		if(verifiySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
				
		//gather transaction inputs (Make sure they are unblocked):
		for(TransactionInput i : inputs) {
			i.UTXO = FishChain.UTXOs.get(i.transactionOutputId);
		}
 
		//check if transaction is valid:
		if(getInputsValue() < FishChain.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: " + getInputsValue());
			return false;
		}
		
		//generate transaction outputs:
		float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
		transactionId = StringToShaCode.doSha256(StringToShaCode.getStringFromKey(sender) +
				StringToShaCode.getStringFromKey(reciepienter) +
				Float.toString(value));
		
		outputs.add(new TransactionOutput( this.reciepienter, value,transactionId)); //add value to recipient
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender		
				
		//add outputs to Unspent list
		for(TransactionOutput o : outputs) {
			FishChain.UTXOs.put(o.id , o);
		}
		
		//remove transaction inputs from UTXO lists as spent:
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			FishChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
//returns sum of inputs(UTXOs) values
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			total += i.UTXO.value;
		}
		return total;
	}
 
//returns sum of outputs:
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;

	}

	public void generateSignature(PrivateKey privateKey) {
		String data = StringToShaCode.getStringFromKey(sender) + StringToShaCode.getStringFromKey(reciepienter) + Float.toString(value)	;
		signature = StringToShaCode.applyECDSASig(privateKey,data);		
	}
	//verifiy Signature
	public boolean verifiySignature() {
		String data = StringToShaCode.getStringFromKey(sender) + StringToShaCode.getStringFromKey(reciepienter) + Float.toString(value)	;
		return StringToShaCode.verifyECDSASig(sender, data, signature);

	}
}

