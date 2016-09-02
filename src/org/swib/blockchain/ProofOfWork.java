package org.swib.blockchain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ブロックの採掘及び検証
 */
public class ProofOfWork {
    private static final int PROOF_PREFIX_LEN = 15;
    private static final int NONCE_LEN = 20;

    /**
     * ブロックを指定回数採掘する。Nonceをランダムで生成し、ハッシュの先頭BitがPROOF_PREFIX_LENだけ0になったら採掘成功
     * 
     * @param block 採掘しようとするブロック(コイン。トランザクション)の情報を含んでいる
     * @param trial 試行回数
     * @return Trueなら成功。block.nonceには成功したNonceが記録されている。
     */
    public static boolean find(Block block, int trial) {
        if (trial < 0) trial = Integer.MAX_VALUE;
        for (int i = 0; i < trial; i++) {
            if (checkHash(block)) {
                return true;
            }
            block.nonce = Common.encodeHex(Common.getRand(NONCE_LEN));
        }
        return false;
    }

    /**
     * ブロックチェーンをバリデートする。<br>
     * NG項目は下記
     * <ul>
     * <li>1. ハッシュの先頭BitがPROOF_PREFIX_LENだけ0になっていない(採掘に成功していないブロックがチェーン内にある)
     * <li>2. 前のブロックのハッシュがblock.prevHashと異なる値である
     * <li>3. ブロックに含まれるコインが過去に採掘されたものではない
     * <li>4. coin.tx.prevHash が前のトランザクションのハッシュと異なる
     * <li>5. コインの署名がひとつ前のコインの持ち主の公開鍵で検証できない
     * </ul>
     * @param blockList ブロックチェーン
     * @return 0: OK
     */
    public static int validate(List<Block> blockList) {
        Block prevBlock = null;

        Map<String, List<Coin>> coins = new HashMap<>();
        Map<String, Boolean> check = new HashMap<>();
        for (Block block : blockList) {
            if (!checkHash(block)) {
                return 1; // 1.NG
            }

            for (Coin coin : block.coins) {
                if (!coins.containsKey(coin.id)) {
                    coins.put(coin.id, new ArrayList<Coin>());
                }
                coins.get(coin.id).add(coin);
            }
            check.put(block.coinId, true);

            if (prevBlock != null) {
                byte[] preHash = Common.hash(prevBlock);
                if (!Common.encodeHex(preHash).equals(block.prevHash)) {
                    return 2; // 2.NG
                }
            }

            prevBlock = block;
        }

        for (Map.Entry<String, List<Coin>> entry : coins.entrySet()) {
            if (check.get(entry.getKey()) == null) {
                return 3; // 3.NG
            }
            int ret = TransactionOperator.validate(entry.getValue());
            if (ret > 0) {
                return 3 + ret; // 4.NG or 5.NG
            }
        }
        return 0;
    }

    private static boolean checkHash(Block block) {
        byte[] hash = Common.hash(block);
        int n = PROOF_PREFIX_LEN / 8;
        for (int i = 0; i < n; i++) {
            if (hash[i] != 0) {
                return false;
            }
        }
        int m = PROOF_PREFIX_LEN % 8;
        if ((hash[n] & ((1 << m) - 1)) != 0) {
            return false;
        }
        return true;
    }
}
