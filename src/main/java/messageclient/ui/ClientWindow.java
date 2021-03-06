package messageclient.ui;

import messageclient.api.Client;
import messageclient.api.MessageObserver;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ClientWindow extends JFrame implements MessageObserver {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private volatile Client client;
    private final JTextArea textArea;

    public ClientWindow(Client client) {
        super("MessageClient");
        textArea = createTextArea();
        this.client = client;

        add(createScrollableTextArea(textArea), BorderLayout.CENTER);
        add(createTextField(), BorderLayout.SOUTH);

        setSize(600, 800);
        setVisible(true);
        setLocationRelativeTo(null);

        var superThis = this;
        addWindowListener(new WindowAdapter () {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    superThis.client.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public static ClientWindow fromClient(Client client) {
        ClientWindow w = new ClientWindow(client);
        client.register(w);
        return w;
    }


    public void append(String string)  {
        textArea.append(string);
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(FONT);
        textField.addActionListener(e -> {
            var msg = e.getActionCommand();
            try {
                client.sendMessage(msg + "\n");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            textField.setText("");
        });
        return textField;
    }

    private static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(FONT);
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textArea.setCaret(caret);
        return textArea;
    }

    private static JScrollPane createScrollableTextArea(JTextArea textArea) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        return scroll;
    }

    @Override
    public void receivedMessage(String message) {
        append(message);
    }

    @Override
    public void connectionStarted(Client client) {
        if (!isVisible()) setVisible(true);
        this.client = client;
        append("-- Connected to " + client.getAddress() + "\n");
    }

    @Override
    public void connectionClosed() {
        append("-- Connection closed.\n");
    }
}
