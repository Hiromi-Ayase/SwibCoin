package org.swib.blockchain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BitCoinを操作するノード<br>
 * 本来はP2PでSocket通信をするが、今回はオブジェクト間でのbyte[]でのデータ渡しになっている。<br>
 * NodeOperatorはユーザが操作する関数で、コインの採掘(＝トランザクションの保証)、コインの送金、正しくないコインの送金と言った操作ができる
 * <br>
 * RemoteNodeは各Nodeが知っている他のノードで、ノード情報拡散、トランザクションリクエスト、ブロックチェーンの同期という操作ができる
 */
public class Node implements RemoteNode, NodeOperator {
    private final String publicKey;
    private final String name;

    private final String privateKey;
    private final List<RemoteNode> nodes = new ArrayList<>();
    private final List<Block> blockList = new ArrayList<>();
    private final Map<String, List<Coin>> wallet = new HashMap<>();

    /**
     * ノードの作成
     * 
     * @param name ノード名
     */
    public Node(String name) {
        String[] keys = DigitalSign.generateKey();
        this.privateKey = keys[1];
        this.publicKey = keys[0];
        this.name = name;

        log("Client start. - " + publicKey);
        nodes.add(this);
    }

    /**
     * 自分の持っているノード情報を他のノードに拡散する。 通常は数個と接続して、マルチホップ通信で拡散するが、今回は全ノードが互いに接続している。
     * 
     * @param node 追加するノード
     */
    public void addNode(RemoteNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            for (RemoteNode n : nodes) {
                if (node != n) {
                    n.addNode(node);
                    node.addNode(n);
                }
            }
        }
    }

    /**
     * 適当にコインを作ってトランザクションリクエストを出す
     * 
     * @param mode 1:他の人のコインを送る 2:採掘してないコインを送る 3：前のハッシュと異なるコインを送る
     * @param receiverPublicKey 送り先の公開鍵
     * @return True:成功
     */
    public boolean sendWrong(int mode, String receiverPublicKey) {
        Coin coin = null;
        String type = "";

        if (mode == 1) {
            for (String pkey : wallet.keySet()) {
                if (!pkey.equals(publicKey)) {
                    coin = wallet.get(pkey).get(0);
                    type = "Other's coin";
                    break;
                }
            }
        } else if (mode == 2) {
            coin = new Coin(publicKey);
            type = "Not mined coin";
        } else if (mode == 3) {
            if (getWallet() > 0) {
                Coin now = wallet.get(publicKey).get(0);
                Coin.Transaction tx = new Coin.Transaction("aaaaaaaaaa", now.tx.receiverPublicKey);
                coin = new Coin(now.id, tx, now.senderSign);
                type = "Illegal previous transaction hash";
            }
        }

        if (coin != null) {
            log("HACK! Send wrong coin(" + type + ") to " + receiverPublicKey);
            coin = TransactionOperator.send(coin, privateKey, receiverPublicKey);
            requestRandom(coin);
            return true;
        } else {
            err("Illegal Operation!");
            return false;
        }
    }

    /**
     * 誤ったブロックをブロードキャストする
     * 
     * @param mode 1:短いブロックチェーンを送信  2:採掘してないブロックチェーンを送信 3：前のハッシュと異なるブロックチェーンを送信
     * @return True:成功
     */
    public boolean wrongTranaction(int mode) {
        String type = "";
        List<Block> newBlockList = new ArrayList<>(blockList);
        Coin myCoin = new Coin(publicKey);
        Block block;
        if (mode == 1) {
            type = "Too short chain";
            newBlockList.clear();
            block = new Block(null, myCoin);
            ProofOfWork.find(block, -1);
        } else if (mode == 2) {
            block = new Block(getLastBlockHash(), myCoin);
            block.nonce = "aaaaaaaaaa";
            type = "Not mined block";
        } else if (mode == 3) {
            block = new Block("aaaa", myCoin);
            ProofOfWork.find(block, -1);
            type = "Illegal previous block hash";
        } else {
            err("Illegal Operation!");
            return false;
        }
        newBlockList.add(block);
        log("HACK! Broadcast wrong transaction(" + type + ")");
        blockBroadcast(newBlockList);
        return true;
    }

    /**
     * 正当なコインでトランザクションリクエストを出す
     * 
     * @param receiverPublicKey 送り先の公開鍵
     * @return True:成功
     */
    public boolean send(String receiverPublicKey) {
        if (wallet.containsKey(publicKey) && wallet.get(publicKey).size() > 0) {
            log("Send coin to " + receiverPublicKey);
            Coin coin = wallet.get(publicKey).get(0);
            coin = TransactionOperator.send(coin, privateKey, receiverPublicKey);
            requestRandom(coin);
            return true;
        } else {
            err("No coins!");
            return false;
        }
    }

    /**
     * 空のトランザクションを処理する(採掘目的)
     */
    public void mining() {
        log("Trying digging new coin...");
        while (!requestTransaction(null)) {
        }
    }

    /**
     * 今のコインの数(Walletには全ノードのコインの情報が入っている)
     */
    public int getWallet() {
        return this.wallet.containsKey(publicKey) ? this.wallet.get(publicKey).size() : 0;
    }

    /**
     * トランザクションのリクエストを受けた<br>
     * ブロックの採掘を開始し、10000回試行して見つかったらブロックチェーンにつなげてノードにブロードキャスト
     * 
     * @param input ノードから送られてきた情報(コイン)
     */
    public boolean requestTransaction(byte[] input) {
        Coin myCoin = new Coin(publicKey);
        Block block = new Block(getLastBlockHash(), myCoin);

        if (input != null) {
            Coin coin = (Coin) Common.deserialize(input);
            block.coins.add(coin);
        }

        if (ProofOfWork.find(block, 10000)) {
            log("New block found:" + block);
            List<Block> newBlockList = new ArrayList<>(blockList);
            newBlockList.add(block);
            blockBroadcast(newBlockList);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 新しいブロックチェーンを同期する
     * 
     * @param input ノードから送られてきた情報(ブロックチェーン)
     */
    public void syncBlock(byte[] input) {
        @SuppressWarnings("unchecked")
        List<Block> newBlockList = (List<Block>) Common.deserialize(input);
        if (newBlockList.size() <= blockList.size()) {
            err("Block rejected(0. New block length is too short)");
        } else {
            int ret = ProofOfWork.validate(newBlockList);
            Block latest = newBlockList.get(newBlockList.size() - 1);
            if (ret == 0) {
                log("New block accepted: " + latest);
                this.blockList.clear();
                this.blockList.addAll(newBlockList);

                Map<String, String> map = new HashMap<>();
                Map<String, Coin> coins = new HashMap<>();
                for (Block block : newBlockList) {
                    for (Coin coin : block.coins) {
                        map.put(coin.id, coin.tx.receiverPublicKey);
                        coins.put(coin.id, coin);
                    }
                }

                this.wallet.clear();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String pkey = entry.getValue();
                    if (!wallet.containsKey(pkey)) {
                        wallet.put(pkey, new ArrayList<Coin>());
                    }
                    wallet.get(pkey).add(coins.get(entry.getKey()));
                }

            } else {
                String[] messages = { "Not mined block", "Illegal previous block hash", "Not mined coin",
                        "Illegal previous coin hash", "Other's coin" };
                err("Block rejected(" + ret + ". " + messages[ret - 1] + "): " + latest);
            }
        }
    }

    private String getLastBlockHash() {
        return blockList.size() > 0 ? DigitalSign.hash(blockList.get(blockList.size() - 1)) : null;
    }

    private void blockBroadcast(List<Block> newBlockList) {
        for (RemoteNode n : nodes) {
            n.syncBlock(Common.serialize(newBlockList));
        }
    }

    private void requestRandom(Coin coin) {
        while (true) {
            RemoteNode n = this.nodes.get((int) (Math.random() * nodes.size()));
            if (n.requestTransaction(Common.serialize(coin))) {
                break;
            }
        }
    }

    private void log(String message) {
        System.out.println(this.name + "(" + publicKey + "): " + message);
        System.out.flush();
    }

    private void err(String message) {
        System.err.println(this.name + "(" + publicKey + "): " + message);
        System.err.flush();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }
}
