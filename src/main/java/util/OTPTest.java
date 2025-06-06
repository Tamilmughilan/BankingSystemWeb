package util;
import java.util.Random;

public class OTPTest {
    public static void main(String[] args) {
        long seed = System.currentTimeMillis() / (1000 * 60);

        Runnable task = () -> {
            Random random = new Random(seed);
            int otp = random.nextInt(900000) + 100000;
            System.out.println(Thread.currentThread().getName() + " - OTP: " + otp);
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        t2.start();
    }
}
