package org.swib.blockchain;
import java.util.List;

/**
 * コインのトランザクションの検証及び生成
 */
public class TransactionOperator {
    /**
     * コインのトランザクション検証<br>
     * NG要因は下記の通り
     * 
     * <ul>
     * <li> 1. coin.tx.prevHash が前のトランザクションのハッシュと異なる
     * <li> 2. コインの署名がひとつ前のコインの持ち主の公開鍵で検証できない
     * </ul>
     * 
     * @param coins
     * @return
     */
	public static int validate(List<Coin> coins) {
		for (int i = 1; i < coins.size(); i ++) {
			Coin preCoin = coins.get(i - 1);
			Coin coin = coins.get(i);

			String sign = coin.senderSign;
			String publicKey = preCoin.tx.receiverPublicKey;
			
			if (!DigitalSign.hashCheck(preCoin, coin.tx.prevHash)) {
			    return 1;
			} else if (!DigitalSign.validate(coin.tx, sign, publicKey)) {
				return 2;
			}
		}
		return 0;
	}
	
	/**
	 * コインを送る
	 * 
	 * @param coin 送ろうとしている既存のコイン
	 * @param senderPrivateKey 自分の秘密鍵(署名用)
	 * @param receiverPublicKey 送り先の公開鍵
	 * @return 新しいコイン
	 */
	public static Coin send(Coin coin, String senderPrivateKey, String receiverPublicKey) {
		String prevHash = DigitalSign.hash(coin);
		Coin.Transaction tx = new Coin.Transaction(prevHash, receiverPublicKey);
		String sign = DigitalSign.sign(tx, senderPrivateKey);
		Coin newCoin = new Coin(coin.id, tx, sign);
		return newCoin;
	}
}
