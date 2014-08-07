import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

public aspect onEDT {
    pointcut onEDT(): call(@OnEDT * *(..));

    void around(): onEDT() {
        if (!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    proceed();
                }
            });
        }
    }
}