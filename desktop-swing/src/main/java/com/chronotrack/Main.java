package com.chronotrack;

import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.MainFrame;

import javax.swing.*;

/**
 * Main application launcher for ChronoTrack Desktop Swing client and optional embedded Java server.
 */
public class Main {

    public static void main(String[] args) {
        boolean startServerArg = false;
        boolean seedDataArg = false;
        int port = 5000;

        for (int i = 0; i < args.length; i++) {
            if ("--server".equalsIgnoreCase(args[i])) {
                startServerArg = true;
            } else if ("--seed".equalsIgnoreCase(args[i])) {
                seedDataArg = true;
            } else if ("--port".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {}
            }
        }

        final boolean startServer = startServerArg;
        final boolean seedData = seedDataArg;
        final int serverPort = port;

        SwingUtilities.invokeLater(() -> {
            try {
                // Apply System Look and Feel for native OS aesthetics
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Note: Using default look and feel: " + e.getMessage());
            }

            HistoryService historyService = new HistoryService();

            if (seedData) {
                historyService.generateSampleData();
            }

            if (startServer) {
                try {
                    historyService.getEmbeddedServer().start();
                    historyService.setUseEmbeddedServer(true);
                } catch (Exception ex) {
                    System.err.println("Could not start embedded server on port " + serverPort + ": " + ex.getMessage());
                }
            }

            MainFrame frame = new MainFrame(historyService);
            frame.setVisible(true);
        });
    }
}
