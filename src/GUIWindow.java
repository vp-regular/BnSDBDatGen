import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import javax.swing.*;

public class GUIWindow extends JFrame {
    private final JPanel panel;
    private JLabel ipLabel;
    private JTextField ipField;
    private JLabel worldIDLabel;
    private JTextField worldIDField;
    private JLabel passwordLabel;
    private JTextField passwordField;
    private JLabel dbNameLabel;
    private JTextField dbNameField;
    private JLabel managementDBNameLabel;
    private JTextField managementDBNameField;
    private JButton button;
    private JButton infoButton;
    private final String IPV4_PATTERN = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
    private final String NUM_PATTERN = "\\d+";

    public GUIWindow() {
        panel = new JPanel();
        initializeComponents();
        setTitle("BnSDBDatGen");
        setVisible(true);
        setSize(400, 325);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        button.addActionListener(e -> {
            if (!ipField.getText().matches(IPV4_PATTERN)) {
                JOptionPane.showMessageDialog(null, "Invalid IP Address.");
                return;
            }
            if (!worldIDField.getText().matches(NUM_PATTERN)) {
                JOptionPane.showMessageDialog(null, "Invalid World ID.");
                return;
            }
            generateManagementDSN();
            generateDBDSN();
            try {
                Desktop.getDesktop().open(new File("."));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        infoButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "Populate text fields with the indicated information, and click the" +
                "'Generate' button to create managementd_db_dsn.dat and db_dsn.dat. They will be located in the same directory " +
                "that this program is located in.\n Source code can be found at: https://github.com/vp-cshow/BnSDBDatGen"));
        populateJPanel();
        add(panel);
    }

    private void initializeComponents() {
        ipLabel = new JLabel("Enter IP Here:");
        ipField = new JTextField(30);
        worldIDLabel = new JLabel("Enter World ID Here:");
        worldIDField = new JTextField(30);
        passwordLabel = new JLabel("Enter Password Here:");
        passwordField = new JPasswordField(30);
        dbNameLabel = new JLabel("Enter DB Name Here:");
        dbNameField = new JTextField(30);
        managementDBNameLabel = new JLabel("Enter Management DB Name Here:");
        managementDBNameField = new JTextField(30);
        button = new JButton("Generate");
        infoButton = new JButton("Help");
    }

    private void populateJPanel() {
        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(worldIDLabel);
        panel.add(worldIDField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(dbNameLabel);
        panel.add(dbNameField);
        panel.add(managementDBNameLabel);
        panel.add(managementDBNameField);
        panel.add(button);
        panel.add(infoButton);
    }

    private void generateManagementDSN() {
        String fileName = ".\\managementd_db_dsn.dat";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // could probably change the connection string's user and port, but nobody's going to do that more than likely
        String sqlConnectionString =
                "DRIVER={SQL Server};SERVER=" + ipField.getText() + ",1433;UID=sa;PWD=" + passwordField.getText() +
                        ";DATABASE=" + managementDBNameField.getText() + ";";
        buffer.putInt(sqlConnectionString.length() + 12);
        buffer.putInt(1); // unknown
        buffer.putInt(1); // unknown
        buffer.putInt(sqlConnectionString.length());
        buffer.put(sqlConnectionString.getBytes(StandardCharsets.US_ASCII));
        int size = buffer.position();
        buffer.position(0);
        byte[] payload = new byte[size];
        buffer.get(payload, 0, size);
        try {
            Files.write(Paths.get(fileName), payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateDBDSN() {
        String fileName = ".\\db_dsn.dat";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        String sqlConnectionString =
                "DRIVER={SQL Server};SERVER=" + ipField.getText() + ",1433;UID=sa;PWD=" + passwordField.getText() +
                        ";DATABASE=" + dbNameField.getText() + ";";
        buffer.putInt(sqlConnectionString.length() + 12);
        buffer.putInt(1); // unknown
        buffer.putInt(Integer.parseInt(worldIDField.getText()));
        buffer.putInt(sqlConnectionString.length());
        buffer.put(sqlConnectionString.getBytes(StandardCharsets.US_ASCII));
        int size = buffer.position();
        buffer.position(0);
        byte[] payload = new byte[size];
        buffer.get(payload, 0, size);
        try {
            Files.write(Paths.get(fileName), payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}