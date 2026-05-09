/*
 * This is a UDP packet receiver application that:

Listens for UDP packets on a configurable port
Filters packets containing ADIF (Amateur Data Interchange Format) data
Modifies the ADIF data by adding satellite-related fields
Writes the processed data to an ADIF file
It's designed for amateur radio operators to integrate logging data from applications like Log4OM2.

How It Works
GUI Components
Creates a main window with a text area for displaying messages
Includes a settings button that opens a configuration window
Shows status messages and warnings about avoiding duplicate entries
Core Functionality
Configuration Loading: Reads UDP port and file path from config
UDP Listening: Binds to a socket and continuously receives packets
Packet Filtering: Only processes packets containing <STATION_CALLSIGN
Data Processing: Extracts ADIF data and appends satellite fields
File Writing: Appends processed data to an ADIF file with proper headers
* 
*/



// Package declaration for the class
package std;

import java.io.File;
import java.io.FileOutputStream; // For writing to files
import java.io.IOException; // For handling IO exceptions
import java.net.DatagramPacket; // For handling UDP packets
import java.net.DatagramSocket; // For receiving UDP packets
import java.nio.charset.StandardCharsets; // For character encoding
import java.util.Arrays; // For array operations

import javax.swing.ImageIcon; // For displaying images in GUI
import javax.swing.JButton; // For creating buttons
import javax.swing.JFrame; // For creating the main window
import javax.swing.JScrollPane; // For scrollable components
import javax.swing.JTextArea; // For text display/editing
import javax.swing.JLabel; // For displaying text or images
import java.awt.Font; // For font settings
import javax.swing.SwingConstants; // For alignment settings
import java.awt.Color; // For color settings
import java.awt.Desktop;

// Main class declaration
public class UdpToAdifFile {
    // Reference to the configuration window
    private static Configuration secondFrame;
    // Main application window
    private static JFrame frame;
    // Configuration object to hold settings
    public static Config config;
    // String to hold the result of ADIF processing
    public static String result;

    // Suppress warnings for deprecated methods
 
    // Main method: entry point of the application
    public static void main(String[] args) {
    	
  	
        // Create the main application window
        frame = new JFrame();
        // Set the title of the window
        frame.setTitle("ADIF integrator by I2TPY");
        // Load an icon image from resources
        ImageIcon icon = new ImageIcon(
                UdpToAdifFile.class.getResource("/img/I2TPY.png")
        );

        // Set the window icon
        frame.setIconImage(icon.getImage());
        // Set window size and position
        frame.setBounds(100, 100, 900, 550);
        // Set default close operation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set layout manager to null for absolute positioning
        frame.getContentPane().setLayout(null);

        // Make the window visible
        frame.setVisible(true);

        // Create a settings button
        JButton btnSetting = new JButton("...");

        // Add action listener to the settings button
        btnSetting.addActionListener(e -> {

            // Check if the configuration window is null or not displayed
            if (secondFrame == null || !secondFrame.isDisplayable()) {
                // Create and show the configuration window
                secondFrame = new Configuration();
                secondFrame.setVisible(true);
            } else {
                // Bring the configuration window to the front
                secondFrame.toFront();
                secondFrame.requestFocus();
            }

        });
        // Set button position and size
        btnSetting.setBounds(849, 22, 25, 23);
        // Add button to the window
        frame.getContentPane().add(btnSetting);

        // Create a label for the message area
        JLabel lblNewLabel_1 = new JLabel("MESSAGE AREA");
        // Set horizontal alignment
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        // Set font
        lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
        // Set position and size
        lblNewLabel_1.setBounds(342, 31, 176, 31);
        // Add label to the window
        frame.getContentPane().add(lblNewLabel_1);

        // Create a label with a warning message
        JLabel lblNewLabel_2 = new JLabel("Be sure that Log4OM2 is not running to avoid potential duplicated entries");
        // Set font
        lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 13));
        // Set text color
        lblNewLabel_2.setForeground(new Color(3, 27, 241));
        // Set position and size
        lblNewLabel_2.setBounds(10, 461, 487, 25);
        // Add label to the window
        frame.getContentPane().add(lblNewLabel_2);
          
        // Create a new button to close the application and related listener
        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnExit.setBounds(785, 463, 89, 23);
        frame.getContentPane().add(btnExit);
        btnExit.addActionListener(e -> {

            // close sockets if needed

            System.exit(0);
        });
           
        // Create a new button to open the directory of config.json and files using Windows explorer and relative listener
        JButton btnOpenDir = new JButton("OpenDirectory");
        btnOpenDir.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnOpenDir.setBounds(603, 463, 157, 23);
        frame.getContentPane().add(btnOpenDir);
        btnOpenDir.addActionListener(e -> {
        String userHome = System.getProperty("user.home");
            try {
            	File folder = new File(
            			userHome + config.dir
                );

            Desktop.getDesktop().open(folder);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Create a text area for displaying messages
        JTextArea textArea = new JTextArea();
        // Enable line wrapping
        textArea.setLineWrap(true);
        // Make text area non-editable
        textArea.setEditable(false); // or false if only append()

        // Create a scroll pane for the text area
        JScrollPane scroll = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        // Set scroll pane position and size
        scroll.setBounds(10, 64, 864, 386);

        // Add scroll pane to the window
        frame.getContentPane().add(scroll);

        // Create a label for configuration
        JLabel lblNewLabel = new JLabel("Configuration");
        // Set position and size
        lblNewLabel.setBounds(768, 24, 85, 19);
        // Add label to the window
        frame.getContentPane().add(lblNewLabel);
        

 
        // Load configuration settings
        config = Configuration.leggiConfig();

        // Parse UDP port from configuration
        int listenPort;
        try {
            listenPort = Integer.parseInt(config.nUDP);
        }
        // If parsing fails, use default port 9999
        catch (NumberFormatException e) {
            listenPort = 9999;
        }

        // Set append mode for file writing
        boolean appendMode = true;
        // Get user home directory
        String userHome = System.getProperty("user.home");
        // Construct output file path
        String outputFile = userHome + config.dir + "NewAdif.adi";
        
        // Create UDP socket for receiving packets
        try (DatagramSocket receiveSocket = new DatagramSocket(null)) {
            // Enable address reuse
            receiveSocket.setReuseAddress(true);

            // Bind socket to the specified port
            receiveSocket.bind(new java.net.InetSocketAddress(listenPort));

            // Print and display listening message
            System.out.println("Listening UDP on port " + listenPort);
            textArea.append("Listening UDP on port " + listenPort + "\n");

            // Create buffer for receiving packets
            byte[] buffer = new byte[4096];

            // Infinite loop for receiving packets
            while (true) {

                // Create packet to receive data
                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                // Receive packet from socket
                receiveSocket.receive(packet);

                // Copy only the actual received data
                byte[] data = Arrays.copyOf(
                        packet.getData(),
                        packet.getLength()
                );

                // Convert beginning of data to string for filtering
                String start = new String(
                        data,
                        StandardCharsets.US_ASCII
                );
                // Convert to uppercase
                start = start.toUpperCase();
                // Print the start of the packet
                System.out.println(start);

                // Filter packets that don't contain station callsign
                if (!start.contains("<STATION_CALLSIGN")) {

                    // Print and display ignored packet message
                    System.out.println("Ignored UDP packet -> " + start);
                    textArea.append("Ignored UDP packet\n");   // -> " + start + "\n");
                    // Skip to next iteration
                    continue;
                }
                // Modify the received data
                byte[] modified = editBytes(data);

                // Write modified data to file
                writeToFile(outputFile, modified, appendMode);

                // Print and display file update message
                System.out.println("ADIF file updated");
                textArea.append("ADIF file updated\n" + result + "\n");
            }

        } catch (Exception e) {
            // Print stack trace for any exceptions
            e.printStackTrace();
        }
    }

    // Method to edit received byte data
    private static byte[] editBytes(byte[] input) {

        // Convert input bytes to string
        String text = new String(
                input,
                java.nio.charset.StandardCharsets.US_ASCII
        );

        // Convert to lowercase for processing
        String lower = text.toLowerCase();

        // Find start of ADIF data
        int start = lower.indexOf("<adif_ver");
        // If not found, return null
        if (start < 0) {
            return null; // ignore non-ADIF packets
        }

        // Find end of record marker
        int eorPos = lower.indexOf("<eor>", start);
        // If not found, return null
        if (eorPos < 0) {
            return null; // incomplete ADIF
        }

        // Extract ADIF section
        String adif = text.substring(start, eorPos);

        // Prepare satellite data to append
        String satData = config.field1 + config.field2 + config.field3 + config.field4 + config.field5 + config.field6 + config.field7 + config.field8 + config.field9;

        // Create final result string
        result =
                adif
                        + satData
                        + "<EOR>";

        // Convert result to bytes and return
        return result.getBytes(
                java.nio.charset.StandardCharsets.US_ASCII
        );
    }

    // Method to replace first occurrence of a byte sequence

    // Method to write data to file
    private static void writeToFile(
            String filePath,
            byte[] data,
            boolean appendMode) throws IOException {

        // Create file object
        java.io.File file = new java.io.File(filePath);

        // Flag to determine if header should be written
        boolean writeHeader = false;

        // Check if header should be written
        if (appendMode) {

            // If file doesn't exist or is empty, write header
            if (!file.exists() || file.length() == 0) {
                writeHeader = true;
            }
        }

        // Create file output stream with append mode
        try (FileOutputStream fos =
                     new FileOutputStream(filePath, appendMode)) {

            // If header should be written
            if (writeHeader) {

                // Create header string
                String header = "Generated by UDP Logger by I2TPY\r\n" + "<EOH>\r\n";

                // Write header to file
                fos.write(header.getBytes(StandardCharsets.US_ASCII));
            }

            // Write data to file
            fos.write(data);

            // Write final newline for safety
            fos.write("\r\n".getBytes(StandardCharsets.US_ASCII));

            // Flush output stream
            fos.flush();
        }
    }
}
