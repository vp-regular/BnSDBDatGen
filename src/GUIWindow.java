import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import javax.swing.*;

public class GUIWindow extends JFrame {
    private final JPanel panel;
    private final JLabel ipLabel;
    private final JTextField ipField;
    private final JLabel worldIDLabel;
    private final JTextField worldIDField;
    private final JLabel passwordLabel;
    private final JTextField passwordField;
    private final JLabel dbNameLabel;
    private final JTextField dbNameField;
    private final JLabel managementDBNameLabel;
    private final JTextField managementDBNameField;
    private final JButton button;
    private final JLabel doneLabel;
    private final String IPV4_PATTERN = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
    private final String NUM_PATTERN = "\\d+";

    public GUIWindow() {
        panel = new JPanel();
        ipLabel = new JLabel();
        ipField = new JTextField(30);
        worldIDLabel = new JLabel();
        worldIDField = new JTextField(30);
        passwordLabel = new JLabel();
        passwordField = new JPasswordField(30);
        dbNameLabel = new JLabel();
        dbNameField = new JTextField(30);
        managementDBNameLabel = new JLabel();
        managementDBNameField = new JTextField(30);
        button = new JButton();
        doneLabel = new JLabel();
        ipLabel.setText("Enter IP Here:");
        worldIDLabel.setText("Enter World ID Here:");
        passwordLabel.setText("Enter Password Here:");
        dbNameLabel.setText("Enter DB Name Here:");
        managementDBNameLabel.setText("Enter Management DB Name Here:");
        button.setText("Generate");
        doneLabel.setText("Done!");
        doneLabel.setVisible(false);
        setTitle("ServerDBDatGen");
        setVisible(true);
        setSize(400, 400);
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
            doneLabel.setVisible(true);
        });
        populateJPanel();
        add(panel);
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
        panel.add(doneLabel);
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