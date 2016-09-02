package org.swib.blockchain;
/**
 * リモートノードインターフェース
 */
public interface RemoteNode {
	public void syncBlock(byte[] input);
	public boolean requestTransaction(byte[] input);
	public void addNode(RemoteNode node);
}
