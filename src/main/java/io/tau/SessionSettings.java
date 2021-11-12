package io.tau;

import org.libTAU4j.*;
import org.libTAU4j.swig.*;

public final class SessionSettings {

    private static final String DB_DIR = "/.tau";

    private static String deviceID;
    private static byte[] sSeed;
    public static Pair<byte[], byte[]> keypair;
    public static String str_friend;

    static {
        sSeed = Ed25519.createSeed();
        System.out.println("Random seed: " + Utils.toHex(sSeed));
        keypair = Ed25519.createKeypair(sSeed);
		deviceID = Utils.toHex(keypair.first);
        System.out.println("Public key: " + Utils.toHex(keypair.first));
        System.out.println("Private key: " + Utils.toHex(keypair.second));
    }

    private static settings_pack sp;

    private static int sStarting_Port = 6881;

    static {
        sp = new settings_pack();

        // set bootstrap nodes
	    sp.set_str(settings_pack.string_types.dht_bootstrap_nodes.swigValue(), dhtBootstrapNodes());
    }

    public static SessionParams getTauSessionParams() {

        // set listen network interfaces
        // sp.set_str(settings_pack.string_types.listen_interfaces.swigValue(), listenInterfaces());

        // set database dir
        sp.set_str(settings_pack.string_types.db_dir.swigValue(), DB_DIR);

        // set account seed
        sp.set_str(settings_pack.string_types.account_seed.swigValue(), Utils.toHex(sSeed));

        // set device id
        sp.set_str(settings_pack.string_types.device_id.swigValue(), deviceID);

        session_params sparams = new session_params(sp);

        return new SessionParams(sparams);
    }

    public static String dhtBootstrapNodes() {
        StringBuilder sb = new StringBuilder();

        // sb.append("dht.libtorrent.org:25401").append(",");
        // ...
        //sb.append("13.229.53.249:6881");
        //sb.append("tau://83024767468B8BF8DB868F336596C63561265D553833E5C0BF3E4767659B826B@taucoin.ddns.net:6882");
        sb.append("tau://83024767468B8BF8DB868F336596C63561265D553833E5C0BF3E4767659B826B@13.229.53.249:6882");

        return sb.toString();
    }

    private static String listenInterfaces() {
        String interfaces = "";
        final int count = 1;
        int port = 6881;

        for (int i = 1; i < count; i++) {
            interfaces = interfaces + "eth0:" + port + ",";
            port++;
        }

        interfaces = interfaces + "eth0:" + port;

        return interfaces;
    }
}
