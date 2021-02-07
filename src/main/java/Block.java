import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Block {
    @Getter @Setter private String hash;
    @Getter @Setter private String previousHash;
    @Getter @Setter private String data;
    @Getter @Setter private long timeStamp;
    @Getter private int nonce;
    private final AtomicBoolean mined = new AtomicBoolean(false);

    public Block(String data, String previousHash, long timeStamp) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = timeStamp;
        this.hash = calculateBlockHash();
    }

    public String calculateBlockHash() {
        String dataToHash = previousHash + timeStamp + nonce + data;
        byte[] bytes = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex){
            ex.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        if(bytes != null){
            for(byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
        }
        return builder.toString();
    }

    synchronized void incrementNonceSync() {
        nonce = nonce + 1;
    }
    public String multithreadedMineBlock(int prefix) throws ExecutionException, InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(cores / 2);
        String prefixString = getPrefixString(prefix);
        Runnable task = () -> {
            long start, end;
            start = new Date().getTime();
            String localHash = hash;
            while(!mined.get() && !localHash.substring(0, prefix).equals(prefixString)){
                incrementNonceSync();
                localHash = calculateBlockHash();
            }
            if(!mined.get()){
                hash = localHash;
                mined.set(true);
                end = new Date().getTime();
                System.out.println(Thread.currentThread().getId() + " found it in " + (end - start) + "ms");
            }
        };
        List<Future<?>> futureList = new ArrayList<>();
        for(int i = 0; i < cores / 2; i++){
            futureList.add(exec.submit(task));
        }
        for (Future<?> future : futureList){
            future.get();
        }
        return hash;
    }
    public static String getPrefixString(int prefix){
        return new String(new char[prefix]).replace('\0', '0');
    }

    public String mineBlock(int prefix) {
        long start, end;
        start = new Date().getTime();
        String prefixString = new String(new char[prefix]).replace('\0', '0');
        while(!hash.substring(0, prefix).equals(prefixString)){
            nonce++;
            hash = calculateBlockHash();
        }
        end = new Date().getTime();
        System.out.println("Mined in " + (end - start) + "ms");
        return hash;
    }
}
