/**
 * Monitor to synchronize on B*MD+D*MT calculation
 */
public class Mon4 {
    private int Flag = 0;

    public synchronized void wait_calc() {
        while (Flag < MPJ_Lab1.P) {
            try {
                wait();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void signal_calc() {
        Flag += 1;
        notifyAll();
    }
}