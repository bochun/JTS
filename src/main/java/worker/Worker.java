package main.java.worker;


import main.java.drivers.Client;
import main.java.logger.LatencyLogger;
import main.java.logger.ThroughputLogger;
import main.java.properties.TestProperties;

/**
 * Created by oleksandr.gyryk on 10/2/17.
 */
public abstract class Worker implements Runnable{

    protected LatencyLogger latencyLogger;
    protected ThroughputLogger throughputLogger;
    protected Client clientDB;
    private volatile Criteria testCompletedCriteria = new Criteria(false);
    private TestProperties workload;
    protected int statsLimit;


    public Worker(Client client) {
        workload = client.getWorkload();
        statsLimit = Integer.parseInt(workload.get(TestProperties.TESTSPEC_STATS_LIMIT));
        clientDB = client;

    }

    @Override
    public void run(){
        testCompletedCriteria.setIsSatisfied(false);
        int testDuration = Integer.parseInt(workload.get(TestProperties.TESTSPEC_TEST_DURATION));
        Thread timer = new Thread(new Timer(testDuration, testCompletedCriteria));
        timer.start();
        while (true) {
            if (testCompletedCriteria.isSatisfied()) {
                shutDown();
                break;
            }
            runQuery();
        }
    }


    abstract protected void runQuery();
    abstract protected void shutDown();


    class Criteria {

        private boolean isSatisfied;

        public Criteria(boolean isSatisfied) {
            this.isSatisfied = isSatisfied;
        }

        public boolean isSatisfied() {
            return isSatisfied;
        }

        public void setIsSatisfied(boolean satisfied) {
            this.isSatisfied = satisfied;
        }

    }

    class Timer implements Runnable {

        private long timeout;
        private Criteria timedout;

        public Timer(long timeout, Criteria timedout) {
            this.timedout = timedout;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeout * 1000);
                this.timedout.setIsSatisfied(true);
            } catch (InterruptedException e) {
                // Do nothing.
            }
        }

    }

}
