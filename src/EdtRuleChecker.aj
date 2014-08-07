import javax.swing.*;

/**
 * AspectJ code that checks for Swing component methods being executed OUTSIDE the Event-Dispatch-Thread.
 *
 * From Alexander Potochkin's blog post here:
 * http://weblogs.java.net/blog/alexfromsun/archive/2006/02/debugging_swing.html
 *
 */

aspect EdtRuleChecker {
    private boolean isStressChecking = true;

    public pointcut anySwingMethods(JComponent c):
            target(c) && call(* *(..));

    public pointcut threadSafeMethods():
            call(* repaint(..)) ||
                    call(* revalidate()) ||
                    call(* invalidate()) ||
                    call(* getListeners(..)) ||
                    call(* add*Listener(..)) ||
                    call(* remove*Listener(..));

    // calls of any JComponent method, including subclasses
    before(JComponent c): anySwingMethods(c) &&
            !threadSafeMethods() &&
            !within(EdtRuleChecker) {
        if (!SwingUtilities.isEventDispatchThread() && (isStressChecking || c.isShowing())) {
            System.err.println("FIXME in " + thisJoinPoint.getSourceLocation());
            System.err.println(thisJoinPoint.getSignature() + "was called outside of EDT");
            System.err.println();
        }
    }

    // calls of any JComponent constructor, including subclasses
    before(): call(JComponent+.new(..)) {
        if (isStressChecking && !SwingUtilities.isEventDispatchThread()) {
            System.err.println("FIXME in " + thisJoinPoint.getSourceLocation());
            System.err.println(thisJoinPoint.getSignature() + " *constructor* was called outside of EDT");
            System.err.println();
        }
    }

}