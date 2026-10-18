public class InfiniteRuntimeDetector implements Runnable {
    @Override
    synchronized public void run() throws InfiniteRuntimeException {
        try {
            wait(5000);
            throw new InfiniteRuntimeException("Code took too long to run (5000 ms).");
        } catch (InterruptedException e) {
            
        }
    }

    public static void main(String[] args) {
        Thread a = new Thread(new InfiniteRuntimeDetector());
        a.start();
    }
}