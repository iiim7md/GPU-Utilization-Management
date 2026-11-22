package ui;

import javax.swing.*;
import logic.ApplicationService;
import model.AppState;
import model.Student;
import model.User;
import storage.FileDataStore;

public class Main {
    private static ApplicationService service;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FileDataStore store=new FileDataStore("data/state.ser");
                AppState state=store.load();
                service=new ApplicationService(state,store);
                showLogin();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,"Could not load saved data: "+ex.getMessage(),"Storage error",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private static void showLogin(){
        LoginFrame frame=new LoginFrame(service.getAccountService(), Main::showWorkspace,Main::showRegister);
        frame.setVisible(true);
    }
    private static void showRegister(){
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w instanceof LoginFrame) w.dispose();
        }
        JFrame frame=new JFrame("Request student account");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(550,470);frame.setLocationRelativeTo(null);
        frame.setContentPane(new RegisterPanel(service.getAccountService(),()->{frame.dispose();showLogin();}));
        frame.setVisible(true);
        // Persist registration changes when the registration window closes.
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e){service.saveUnchecked();}
        });
    }
    private static void showWorkspace(User user){
        JFrame frame=user.isAdmin()?new AdminFrame(service,user,Main::showLogin)
                :new StudentFrame(service,(Student)user,Main::showLogin);
        frame.setVisible(true);
    }
}
