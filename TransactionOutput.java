import java.security.PublicKey;


public class TransactionOutput {
	public String id;
	public PublicKey reciepienter; 
	public float value; 
	public String parentTransactionId; 
	
	
	public TransactionOutput(PublicKey reciepienter, float value, String parentTransactionId) {
		this.reciepienter = reciepienter;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringToShaCode.doSha256(StringToShaCode.getStringFromKey(reciepienter)+Float.toString(value)+parentTransactionId);
	}
}
