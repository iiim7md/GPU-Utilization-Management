package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.*;
import logic.ApplicationService;
import model.Student;
import model.TaskSize;
import model.TimeAllowance;

/** Student workspace with allowance information, submission and private job history. */
public class StudentFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ApplicationService service;
    private final Student student;
    private final JLabel balance = new JLabel();
    private final MyJobsPanel myJobs;

    public StudentFrame(ApplicationService service, Student student, Runnable logout) {
        this.service=service; this.student=student;
        setTitle("GPU Utilization Management - Student");
        setSize(950,600); setLocationRelativeTo(null);setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel header=new JPanel(new BorderLayout());
        header.add(new JLabel("Welcome, "+student.getFullName()+" ("+student.getUniversityId()+")"),BorderLayout.WEST);
        JButton out=new JButton("Log out");out.addActionListener(e->{dispose();logout.run();});
        header.add(out,BorderLayout.EAST);add(header,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        JPanel home=new JPanel(new BorderLayout(10,10));home.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JPanel fields=new JPanel(new GridLayout(5,2,8,8));
        JTextField title=new JTextField(20),app=new JTextField(20);
        JTextArea description=new JTextArea(4,30);
        JComboBox<TaskSize> size=new JComboBox<>(TaskSize.values());
        JLabel duration=new JLabel();
        size.addActionListener(e->duration.setText(((TaskSize)size.getSelectedItem()).getDurationMinutes()+" minutes"));
        fields.add(new JLabel("Job title"));fields.add(title);
        fields.add(new JLabel("Application name"));fields.add(app);
        fields.add(new JLabel("Description"));fields.add(new JScrollPane(description));
        fields.add(new JLabel("Task size"));fields.add(size);
        fields.add(new JLabel("Duration"));fields.add(duration);
        size.setSelectedIndex(0); duration.setText(((TaskSize)size.getSelectedItem()).getDurationMinutes()+" minutes");
        home.add(balance,BorderLayout.NORTH);home.add(fields,BorderLayout.CENTER);
        JButton submit=new JButton("Submit job");
        submit.addActionListener(e->{
            TaskSize selected=(TaskSize)size.getSelectedItem();
            int answer=JOptionPane.showConfirmDialog(this,"Submit "+selected+" job for " +selected.getDurationMinutes()+" minutes?","Confirm",JOptionPane.YES_NO_OPTION);
            if(answer!=JOptionPane.YES_OPTION)return;
            try {
                service.submitJob(student.getUniversityId(),title.getText(),app.getText(),description.getText(),selected);
                title.setText("");app.setText("");description.setText("");refresh();
                JOptionPane.showMessageDialog(this,"Job submitted successfully.");
            }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Submission error",JOptionPane.ERROR_MESSAGE);}
        });
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh=new JButton("Refresh");refresh.addActionListener(e->refresh());
        buttons.add(refresh);buttons.add(submit);home.add(buttons,BorderLayout.SOUTH);
        myJobs=new MyJobsPanel(service,student.getUniversityId());
        tabs.addTab("Home and submit",home);tabs.addTab("My jobs",myJobs);
        tabs.addChangeListener(e->refresh());add(tabs,BorderLayout.CENTER);refresh();
    }
    private void refresh(){
        TimeAllowance a=student.getAllowance();
        balance.setText("Allocated: "+a.getAllocated()+"    Used: "+a.getUsed()+"    Reserved: "+a.getReserved()+"    Available: "+a.getAvailable());
        myJobs.refresh();
    }
}
