import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestChain {
    List<Block> blockchain = new ArrayList<>();
    int prefix = 4;
    String prefixString = new String(new char[prefix]).replace('\0', '0');

    @Before
    public void setUp() {
        Block firstBlock = new Block("START", "0", new Date().getTime());
        try {
            firstBlock.multithreadedMineBlock(prefix);
            blockchain.add(firstBlock);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenBlockchain_whenNewBlockAdded_thenSuccess() {
        Block newBlock = createNewBlock();
        String hash = newBlock.mineBlock(prefix);
        System.out.println("Hash found: " + hash);
        assertEquals(newBlock.getHash().substring(0, prefix), prefixString);
        blockchain.add(newBlock);
   }

   @Test
   public void givenBlockchain_whenValidated_thenSuccess() {
        Block newBlock = createNewBlock();
        newBlock.mineBlock(prefix);
        blockchain.add(newBlock);
        assertEquals(newBlock.getHash().substring(0, prefix), prefixString);
        boolean flag = true;
        for(int i = 0; i < blockchain.size(); i++){
            String previousHash = i==0 ? "0" : blockchain.get(i - 1).getHash();
            flag = blockchain.get(i).getHash().equals(blockchain.get(i).calculateBlockHash())
                    && previousHash.equals(blockchain.get(i).getPreviousHash())
                    && blockchain.get(i).getHash().substring(0, prefix).equals(prefixString);
            if(!flag) break;
        }
        assertTrue(flag);
   }
   @Test
   public void testMultiThreadedMining() {
        Block newBlock = createNewBlock();
        try {
           newBlock.multithreadedMineBlock(prefix);
        } catch (ExecutionException | InterruptedException e) {
           e.printStackTrace();
           assert(false);
        }
        System.out.println("Found: " + newBlock.getHash());
        assertEquals(newBlock.getHash().substring(0, prefix), prefixString);
   }
    @Test
    public void testFirstTenBlocks() {
        int coins = 10;
        while(blockchain.size() < coins){
            Block newBlock = createNewBlock();
            try {
                newBlock.multithreadedMineBlock(prefix);
                blockchain.add(newBlock);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(blockchain.size(), coins);
    }
   @Test
   public void testFirstHundredBlocks() {
        while(blockchain.size() < 100){
            Block newBlock = createNewBlock();
            try {
                newBlock.multithreadedMineBlock(prefix);
                blockchain.add(newBlock);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(blockchain.size(), 100);
   }

   public Block createNewBlock(){
        return new Block("This is a new Block.",
                blockchain.get(blockchain.size() - 1).getHash(),
                new Date().getTime()
        );
   }
}
