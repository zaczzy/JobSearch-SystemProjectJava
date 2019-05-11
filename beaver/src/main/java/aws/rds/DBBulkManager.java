package aws.rds;
import model.Word;
import model.WordEntry;
import org.apache.storm.shade.com.google.common.collect.ImmutableList;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;

import java.sql.PreparedStatement;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class DBBulkManager {


    /************************************
     *  Singleton Instance of DBManager *
     */
    private static DBBulkManager instance;

    public static DBBulkManager getInstance() {
        if (instance == null) {
            instance = new DBBulkManager();
        }
        return instance;
    }

    /**
     * Private Fields
     */
    private DBWorker[] workerPool;
    private CommitQueue commitQueue;
    private boolean started;

    /**
     *  Configure Instance
     */
    private DBBulkManager() {
        workerPool = new DBWorker[128];
        for (int i = 0; i < workerPool.length; i ++) {
            workerPool[i] = new DBWorker();
        }
        commitQueue = new CommitQueue();
    }

    /******************
     * PUBLIC METHODS *
     */

    /**
     * start the worker threads of the DBManager
     */
    public void start() {
        if (!started) {
            for (DBWorker worker : workerPool) { worker.start(); }
            this.started = true;
        }
    }

    /**
     * Insert a record into the database
     * @param entries -- a list of Word to insert to DB
     */
    public void bulkInsert(List<WordEntry> entries) {
        ImmutableList<WordEntry> insert = ImmutableList.copyOf(entries);
        commitQueue.add(insert);
    }

    /**
     * shut the DBManager Instance Down
     * Notify all workers to shutdown.
     */
    public void shutDown() {
        started = false;
        for (DBWorker w : workerPool) {
            if (w.getState() == Thread.State.WAITING) {
                w.interrupt();
            }
        }
    }

    /*****************************
     * PRIVATE CLASS and METHODS *
     */

    /**
     * A queue for commits
     */
    private class CommitQueue {
        Queue<ImmutableList<WordEntry>> queue = new ArrayDeque<>();
        public synchronized void add(ImmutableList<WordEntry> entries) {
            queue.add(entries);
            notifyAll();
        }
        public synchronized ImmutableList<WordEntry> poll() {
            while (started) {
                if (!queue.isEmpty()) {
                    return queue.poll();
                } else {
                    try {
                        wait();
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Thread for doing database transaction.
     */
    private class DBWorker extends Thread {
        @Override
        public void run() {
            Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
            while (started) {
                List<WordEntry> entries = commitQueue.poll();
                if (entries != null && entries.size() > 0) {
                    try {
                        Base.openTransaction();
                        PreparedStatement ps = Base.startBatch(
                                "INSERT INTO words(word, docid, hits, tf, pagerank, wtf, normalizer) " +
                                                        "VALUES(?, ?, ?, ?, ?, ?, ?)");
                        for (WordEntry w : entries) {
                            Base.addBatch(ps, w.getWord(), w.getDocid(), w.getHits(),
                                    w.getTf(), w.getPagerank(), w.getWtf(), w.getNormalizer());
                        }
                        Base.executeBatch(ps);
                        ps.close();
                        Base.commitTransaction();
                        System.out.println("[ 💾 Saved: ] Saved for document " + entries.get(0).getDocid());
                    } catch (Exception e) {
                        Base.rollbackTransaction();
                        System.out.println("[ ⛔️ Failed Transaction: ] Rolled back for " + entries.get(0).getDocid());
                        e.printStackTrace();
                    }
                }
            }
            Base.close();
        }
    }
}
