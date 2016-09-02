package org.swib.blockchain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ブロック。コインのトランザクションを複数含む
 */
public class Block implements Serializable {
    /**
     * Nonce. これを適切な値にすることによりこのブロックが各ノードで承認される
     */
    public String nonce;

    /**
     * 前のブロックのハッシュ
     */
    public final String prevHash;
    
    /**
     * このブロック生成時に採掘したコインID
     */
    public final String coinId;
    
    /**
     * このブロックが保証するトランザクション
     */
    public final List<Coin> coins = new ArrayList<>();

    /**
     * コンストラクタ
     * 
     * @param prevHash 前のブロックのハッシュ
     * @param coin このブロック生成時に採掘したコイン(報酬)
     */
    public Block(String prevHash, Coin coin) {
        this.prevHash = prevHash;
        this.coinId = coin.id;
        coins.add(coin);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hash: " + DigitalSign.hash(this) + ", ");
        sb.append("nonce:" + nonce + ", ");
        sb.append("Prev Hash:" + prevHash + ", ");
        //sb.append(" Coins: " + coins);
        return sb.toString();
    }
}
