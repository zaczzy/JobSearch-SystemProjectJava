package aws.rds;

import Models.Keyword;
import org.apache.storm.shade.com.google.common.collect.ImmutableList;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;

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
        workerPool = new DBWorker[32];
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
    public void bulkInsert(List<Keyword> entries) {
        ImmutableList<Keyword> insert = ImmutableList.copyOf(entries);
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
        Queue<ImmutableList<Keyword>> queue = new ArrayDeque<>();
        public synchronized void add(ImmutableList<Keyword> entries) {
            queue.add(entries);
            notifyAll();
        }
        public synchronized ImmutableList<Keyword> poll() {
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
            DB local = new DB("keywords").open(Credentials.Local.jdbcDriver, Credentials.Local.dbUrl, Credentials.Local.dbUser, Credentials.Local.dbUserPW);
            while (started) {
                List<Keyword> entries = commitQueue.poll();
                if (entries != null && entries.size() > 0) {
                    try {
                        local.openTransaction();
                        PreparedStatement ps = local.startBatch(
                                "INSERT INTO keywords(word, docid, hits, tf, pagerank, wtf, normalizer, url) " +
                                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                        for (Keyword w : entries) {
                            local.addBatch(ps, w.word, w.docId, w.hits,
                                    w.tf, w.pagerank, w.wtf, w.normalizer, w.url);
                        }
                        local.executeBatch(ps);
                        ps.close();
                        local.commitTransaction();
                        System.out.println("[ 💾 Saved: ] Saved ");
                    } catch (Exception e) {
                        local.rollbackTransaction();
                        System.out.println("[ ⛔️ Failed Transaction: ] Rolled back");
                        e.printStackTrace();
                    }
                }
            }
            local.close();
        }
    }
}
