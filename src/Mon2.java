/**
 * Monitor to synchronize on max(MD)*(MT+MZ) calculation
 */
public class Mon2 {
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