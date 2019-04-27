package aws.rds;
import model.Word;
import org.javalite.activejdbc.Base;
import java.util.ArrayDeque;
import java.util.Queue;

public class DBManager {


    /************************************
     *  Singleton Instance of DBManager *
     */
    private static DBManager instance;

    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
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
    private DBManager() {
        workerPool = new DBWorker[4];
        for (int i = 0; i < 4; i ++) {
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
     * @param word -- varchar(64) field
     * @param docId -- varchar(64) field
     * @param hits -- varchar(16*1024) field
     * @param tf -- int field
     */
    public void addRecord(String word, String docId, String hits, int tf) {
        synchronized (commitQueue) {
            commitQueue.add(new Commit(word, docId, hits, tf));
        }
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
     * Data object for a single entry
     */
    private class Commit {
        String word, docId, hits; int tf;
        public Commit(String word, String docId, String hits, int tf) {
            this.word = word; this.docId = docId; this.hits = hits;
            this.tf = tf;
        }
    }

    /**
     * A queue for commits
     */
    private class CommitQueue {
        Queue<Commit> queue = new ArrayDeque<>();
        public synchronized void add(Commit c) {
            queue.add(c);
            notifyAll();
        }
        public synchronized Commit poll() {
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
                Commit commit = commitQueue.poll();
                if (commit != null) {
                    Word.createIt("word", commit.word,
                                  "docId", commit.docId,
                                  "hits", commit.hits,
                                  "tf", commit.tf);
                }
            }
            Base.close();
        }
    }
}
