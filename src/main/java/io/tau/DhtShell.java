package io.tau;

import org.libTAU4j.Account;
import org.libTAU4j.AlertListener;
import org.libTAU4j.Block;
import org.libTAU4j.Ed25519;
import org.libTAU4j.Entry;
import org.libTAU4j.Hex;
import org.libTAU4j.Pair;
import org.libTAU4j.SessionManager;
import org.libTAU4j.SessionParams;
import org.libTAU4j.Sha1Hash;
import org.libTAU4j.Transaction;
import org.libTAU4j.Vectors;
import org.libTAU4j.alerts.*;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

import io.tau.type.Message;
import io.tau.util.CryptoUtil;
import io.tau.util.HashUtil;

import static org.libTAU4j.Vectors.byte_vector2bytes;

/**
 * @author aldenml
 */
public final class DhtShell {

    public static void main(String[] args) {
        SessionManager s = new SessionManager(true);

        AlertListener mainListener = new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();

                if (type == AlertType.LISTEN_SUCCEEDED) {
                    ListenSucceededAlert a = (ListenSucceededAlert) alert;
                    log(a.message());
                    log("==============Bind Interfaces ===============");
                    log("IP: " + a.address().toString() + ", Port: " + a.port());
                    log("=============================================");
                } else if (type == AlertType.LISTEN_FAILED) {
                    ListenFailedAlert a = (ListenFailedAlert) alert;
                    log(a.message());
                } else if (type == AlertType.LOG) {
                    LogAlert a = (LogAlert) alert;
                    log(a.message());
                } /*else if (type == AlertType.DHT_LOG) {
                    DhtLogAlert a = (DhtLogAlert) alert;
                    log(a.message());
                } else if (type == AlertType.SES_START_OVER) {
                    SessionStartOverAlert a = (SessionStartOverAlert) alert;
                    log("============================");
                    log(a.message());
                    log("============================");
                } else if (type == AlertType.DHT_PKT) {
                    DhtPktAlert a = (DhtPktAlert) alert;
                    log(a.message());
                } else if (type == AlertType.COMM_NEW_DEVICE_ID) {
                    CommNewDeviceIdAlert a = (CommNewDeviceIdAlert) alert;
                    log(a.message()); 
               } else if (type == AlertType.COMM_FRIEND_INFO) {
                    CommFriendInfoAlert a = (CommFriendInfoAlert) alert;
                    log(a.message());

                    byte[] encode = Vectors.byte_vector2bytes(a.swig().get_friend_info());
                    if (encode.length > 0) {
                        FriendInfo friendInfo = new FriendInfo(encode);
                        byte[] friend = friendInfo.getPubKey();
                        add_new_friend(s, Utils.toHex(friend));

                        update_friend_info(s, Utils.toHex(friend), encode);
                    }

                } else if (type == AlertType.COMM_LOG) {
                    CommLogAlert a = (CommLogAlert) alert;
                    log(a.message());
                } */
            }
        };

        s.addListener(mainListener);
        s.start(SessionSettings.getTauSessionParams());

        ArrayList activeFriends = new ArrayList<String>();
        Pair<byte[], byte[]> keypair1 = Ed25519.createKeypair(Ed25519.createSeed());
        byte[] friendPubkeyArray = keypair1.first;
        activeFriends.add(Utils.toHex(friendPubkeyArray));
        Pair<byte[], byte[]> keypair2 = Ed25519.createKeypair(Ed25519.createSeed());
        friendPubkeyArray = keypair2.first;
        activeFriends.add(Utils.toHex(friendPubkeyArray));
        Pair<byte[], byte[]> keypair3 = Ed25519.createKeypair(Ed25519.createSeed());
        friendPubkeyArray = keypair3.first;
        activeFriends.add(Utils.toHex(friendPubkeyArray));
        Pair<byte[], byte[]> keypair = Ed25519.createKeypair(Ed25519.createSeed());
        friendPubkeyArray = keypair.first;
        byte[] nickname = Utils.fromHex("tester01");
        BigInteger timestamp = new BigInteger("20190714");
        String friend = Utils.toHex(friendPubkeyArray);
        FriendInfo fi = new FriendInfo(friendPubkeyArray, nickname, timestamp);

		String communityName = "TauTest";
		byte[] chainID = null;
		
		Account account = new Account(1000, 1000, 0);
		Map<String, Account> accounts = new HashMap<String, Account>();
		accounts.put(friend, account);
		
        while (true) {

            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            count_nodes(s);

            //Communication APIs
            //sendNewMessage(s);
            //update_account_seed(s, Ed25519.createSeed());
            //add_new_friend(s, friend);
            //update_friend_info(s, friend, fi.getEncoded());
            //get_friend_info(s, friend);
            //delete_friend(s, friend);
            //set_chatting_friend(s, friend);
            //unset_chatting_friend(s);
            //add_new_msg(s, Utils.fromHex("a123df3a123e221a")); //test for data 
            //set_active_friends(s, activeFriends);

            //Blockchain APIs
            chainID = create_chain_id(s, communityName);
        	log("Getting Chain ID info: " + Utils.toHex(chainID));
            create_new_community(s, chainID, accounts);
			Set<String> peers = new HashSet<String>();	
		    peers.add(friend);
			follow_chain(s, chainID, peers);
			//unfollow_chain(s, chainID);
			Transaction tx = new Transaction(chainID, 0, 1637114512, 
							 friendPubkeyArray, friendPubkeyArray, 100, 100, 10,
            				 Utils.fromHex("a123df3a123e221a"));
			submit_transaction(s, tx);
			
			Account act = get_account_info(s, chainID, friend);
        	log("Account balance: " + act.getBalance());
        	log("Account nonce: " + act.getNonce());

			ArrayList<Block> blks = get_top_tip_block(s, chainID, 0);

			long fee = 10;
			fee = get_median_tx_fee(s, chainID);
        	log("Chain fee: " + fee);

        }
    }

    private static void print(String s, boolean dollar) {
        System.out.println();
        System.out.println(s);
        if (dollar) {
            System.out.print("$ ");
        }
    }

    private static void print(String s) {
        print(s, false);
    }

    private static void log(String s) {
        print(s, true);
    }

    private static boolean is_quit(String s) {
        s = s.split(" ")[0];
        return s.equals("quit") || s.equals("exit") || s.equals("stop");
    }

    private static void quit(SessionManager s) {
        print("Exiting...");
        byte[] data = s.saveState();
        try {
            Files.write(Paths.get("dht_shell.dat"), data);
        } catch (Throwable e) {
            print(e.getMessage());
        }
        s.stop();
        System.exit(0);
    }

    private static void count_nodes(SessionManager s) {
        log("DHT contains " + s.stats().dhtNodes() + " nodes");
    }

    /*
    private static Message sendNewMessage(SessionManager s) {
        byte[] content = BigInteger.valueOf(System.currentTimeMillis() / 1000).toByteArray();
        byte[] logicHash = HashUtil.sha256hash(content);
        byte[] sender = SessionSettings.keypair.first;
        byte[] receiver = SessionSettings.friend;
        byte[] key = CryptoUtil.keyExchange(receiver, SessionSettings.keypair.second);
        Message message = Message.createTextMessage(BigInteger.valueOf(System.currentTimeMillis() / 1000),
                sender, receiver, logicHash, BigInteger.ZERO, content);
        try {
            message.encrypt(key);
        } catch (Exception e) {
            log(e.getMessage());
        }
        log("Send message:" + message.toString());

        //s.addNewMsg(message.getEncoded());

        return message;
    }
    */
    private static void add_new_friend(SessionManager s, String k) {
        log("Add New Friend: " + k);
        s.addNewFriend(k);
    }

    private static void delete_friend(SessionManager s, String k) {
        log("Delete Friend: " + k);
        s.deleteFriend(k);
    }

    private static void set_chatting_friend(SessionManager s, String k) {
        log("Set Chatting Friend: " + k);
        s.setChattingFriend(k);
    }

    private static void unset_chatting_friend(SessionManager s) {
        log("Unset Chatting Friend");
        s.unsetChattingFriend();
    }

    private static void add_new_msg(SessionManager s, byte[] msg) {
        log("Add New Msg: " + Utils.toHex(msg));
        //s.addNewMsg(msg);
    }

    private static void set_active_friends(SessionManager s, ArrayList<String> friends) {
        for(int i = 0 ; i < friends.size(); i++)
        {
            log("Set Active Friend: " + friends.get(i));
        }
        s.setActiveFriends(friends);
    }

    private static void update_friend_info(SessionManager s, String k, byte[] info) {
        log("update Friend info: " + Utils.toHex(info));
        s.updateFriendInfo(k, info);
    }

    private static void get_friend_info(SessionManager s, String k) {
        log("Start Get Friend Info");
        byte[] info = s.getFriendInfo(k);
        log("Getting Friend info: " + Utils.toHex(info));
    }

    private static byte[] create_chain_id(SessionManager s, String c) {
        log("Start Create Chain ID: " + c);
        byte[] chain_id = s.createChainID(c);
		return chain_id;
    }

    private static void create_new_community(SessionManager s, byte[] id, Map<String, Account> accounts) {
        log("Start Create New Community");
        s.createNewCommunity(id, accounts);
        log("End Create New Community");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void follow_chain(SessionManager s, byte[] id, Set<String> peers) {
        log("Start Follow Chain");
        s.followChain(id, peers);
        log("End Follow Chain");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void unfollow_chain(SessionManager s, byte[] id) {
        log("Start UnFollow Chain");
        s.unfollowChain(id);
        log("End UnFollow Chain");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void update_account_seed(SessionManager s, byte[] seed) {
        log("Start Update Account Seed");
        s.updateAccountSeed(seed);
    }

    private static void submit_transaction(SessionManager s, Transaction tx) {
        log("Start Submit Transaciton");
        s.submitTransaction(tx);
        log("End Submit Transaciton");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Account get_account_info(SessionManager s, byte[] id, String pubkey) {
        log("Start Get Account Info");
	    return s.getAccountInfo(id, pubkey);
    }

    private static ArrayList<Block> get_top_tip_block(SessionManager s, byte[] id, int num) {
        log("Start Get Top And Tip Block");
	    return s.getTopTipBlock(id, num);
    }

    private static long get_median_tx_fee(SessionManager s, byte[] id) {
        log("Start Get Median Tx Fee");
	    return s.getMedianTxFee(id);
    }

    private static boolean is_invalid(String s) {
        return !s.isEmpty();
    }

    private static void invalid(String s) {
        print("Invalid command: " + s + "\n" + "Try ? for help");
    }
}
