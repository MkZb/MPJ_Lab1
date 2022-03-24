/**
 * Monitor to synchronize on max(MD) value search
 */
public class Mon1 {
    private int Flag = 0;

    public synchronized void wait_a() {
        while (Flag < MPJ_Lab1.P) {
            try {
                wait();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void signal_a() {
        Flag += 1;
        notifyAll();
    }
}