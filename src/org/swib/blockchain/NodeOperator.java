package org.swib.blockchain;
/**
 * ノードの操作インターフェース
 */
public interface NodeOperator {
	public void addNode(RemoteNode node);
	public boolean sendWrong(int mode, String receiverPublicKey);
	public boolean send(String receiverPublicKey);
	public boolean wrongTranaction(int mode);
	public void mining();
	public int getWallet();
	public String getPublicKey();
	public String getName();
}
