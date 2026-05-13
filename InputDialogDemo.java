import javax.swing.JOptionPane;

public class InputDialogDemo {
    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog(null, "What is your name?");
        JOptionPane.showMessageDialog(null, "Welcome, " + name + ", to Java Programming!");
    }
}