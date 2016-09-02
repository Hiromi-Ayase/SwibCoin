import java.util.Scanner;

import org.swib.blockchain.Node;
import org.swib.blockchain.NodeOperator;

/**
 * Mainクラス
 */
public class Main {
	
	private static NodeOperator[] nodes;
	private static Scanner scanner = new Scanner(System.in);
	private static boolean auto = false;

	public static void main(String[] args) {
	    int n = 4;
	    if (args.length > 0) {
	        try {
	            n = Integer.parseInt(args[0]);
	        } catch(NumberFormatException e) {
	            // None
	        }
	    }
	    if (n < 0) {
	        auto = true;
	        n = -n;
	    }
        init(n);
		
		int[] input;
		int count = 0;
		while (true) {
		    System.out.println("******* Turn " + ++ count + " start. *******");
		    input = getInput();
            if (input[0] == 0) break;
			turn(input[0], input[1], input[2]);
		}
		scanner.close();
	}

	private static void init(int n) {
		nodes = createNode(n);
		digit();
		System.out.println();
	}

	private static int[] getInput() {
        System.out.println("操作を入力してください");
        System.out.println(" 1: ブロックの採掘");
        System.out.println(" 2: コインを送る");
        System.out.println(" 3: 他人のコインを送る");
        System.out.println(" 4: 採掘してない適当なコインを送る");
        System.out.println(" 5: 前のハッシュをいじったコインを送る");
        System.out.println(" 6: 短いブロックチェーンを送る");
        System.out.println(" 7: 採掘してないブロックチェーンを送る");
        System.out.println(" 8: 前のハッシュをいじったブロックチェーンを送る");
        System.out.println(" 0: 終わる");
        System.out.print("> ");

	    int mode = getInput(1, 8);
	    
	    int node = -1;
	    int target = -1;
	    
	    if (mode > 0) {
            while (node < 0 || node >= nodes.length) {
                System.out.println("行動するノードは？");
                System.out.print("> ");
                node =  getInput(0, nodes.length);
            }
	    } else {
	        node = 0;
	    }

	    if (mode == 2 || mode == 3 || mode == 4 || mode == 5) {
	        while (target < 0 || target >= nodes.length) {
                System.out.println("送信先のノードは？");
                System.out.print("> ");
                target =  getInput(0, nodes.length);
	        }
        } else {
            target = 0;
	    }
        
        return new int[]{mode, node, target};
	}
	
	private static int getInput(int base, int n) {
	    if (auto) {
	        int v = base + (int)(Math.random() * n);
	        System.out.println(v);
	        return v;
	    } else {
	        return scanner.nextInt();
	    }
	}

	private static void turn(int mode, int n, int t) {
		NodeOperator node = nodes[n];
		NodeOperator target = nodes[t];

		if (mode == 1) {
		    node.mining();
        } else if (mode == 2) {
            node.send(target.getPublicKey());
        } else if (mode == 3) {
            node.sendWrong(1, target.getPublicKey());
        } else if (mode == 4) {
            node.sendWrong(2, target.getPublicKey());
        } else if (mode == 5) {
            node.sendWrong(3, target.getPublicKey());
        } else if (mode == 6) {
            node.wrongTranaction(1);
        } else if (mode == 7) {
            node.wrongTranaction(2);
        } else if (mode == 8) {
            node.wrongTranaction(3);
		}

		digit();
		System.out.println();
	}

	
	private static void digit() {
		StringBuilder sb = new StringBuilder();
		for (NodeOperator node: nodes) {
			sb.append(node.getName());
			sb.append(":");
			sb.append(node.getWallet());
			sb.append(" ");
		}
		System.out.println(sb);
	}

	private static Node[] createNode(int n) {
		Node[] list = new Node[n];
		for (int i = 0; i < n; i ++) {
			String name = "Node" + i;
			Node node = new Node(name);
			list[i] = node;
			
			if (i > 0) {
				list[0].addNode(node);
			}
		}
		return list;
	}
	
}
