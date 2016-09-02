package org.swib.blockchain;
import java.io.Serializable;

/**
 * コインクラス
 */
public class Coin implements Serializable {

    /**
     * コインの取引を証明する単位
     */
    public static class Transaction implements Serializable {
        /** ひとつ前のこのコインのトランザクションのハッシュ。最初に採掘された時は空文字 */
        public String prevHash;
        /** このトランザクションによってコインを受け取った人のPublic key */
        public String receiverPublicKey;

        public Transaction(String prevHash, String receiverPublicKey) {
            this.prevHash = prevHash;
            this.receiverPublicKey = receiverPublicKey;
        }
    }

    /** このコインのトランザクション(同じIDのトランザクションは過去にも大量にある)   */
    public final Transaction tx;
    /** このコインのID */
    public final String id;
    /** このコインのトランザクションの送り主によるtxの署名 */
    public final String senderSign;

    /**
     * 既存のコインを別の人に送るときに使うコンストラクタ
     * @param id 既存のコインのID
     * @param tx 新しいトランザクション(新しい送り主とひとつ前のトランザクションのハッシュを持つ)
     * @param senderSign 自分の署名(自分が送ったということを証明する)
     */
    public Coin(String id, Transaction tx, String senderSign) {
        this.tx = tx;
        this.senderSign = senderSign;
        this.id = id;
    }

    /**
     * コインを採掘した時に使うコンストラクタ。ただし、IDがBlock.coinIdに記録されていないものはvalidationで無効になるよ
     * @param publicKey 自分のPublic Key(Block生成の報酬なので、自分自身が送り主)
     */
    public Coin(String publicKey) {
        this.id = Common.encodeHex(Common.getRand(20));
        this.tx = new Transaction("", publicKey);
        this.senderSign = "";
    }

    public String toString() {
        return String.format("{Hash: %s, Sign: %s, Receive: %s, PrevHash: %s}",
                DigitalSign.hash(this), senderSign, tx.receiverPublicKey, tx.prevHash);
    }
}
