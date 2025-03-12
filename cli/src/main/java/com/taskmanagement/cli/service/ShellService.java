package com.taskmanagement.cli.service;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShellService {

    @Autowired
    private Terminal terminal;

    public void printSuccess(String message) {
        terminal.writer().println("\u001B[32m" + message + "\u001B[0m");
        terminal.flush();
    }

    public void printError(String message) {
        terminal.writer().println("\u001B[31m" + message + "\u001B[0m");
        terminal.flush();
    }

    public void printInfo(String message) {
        terminal.writer().println("\u001B[36m" + message + "\u001B[0m");
        terminal.flush();
    }

    public void printWarning(String message) {
        terminal.writer().println("\u001B[33m" + message + "\u001B[0m");
        terminal.flush();
    }

    public void printHeading(String heading) {
        terminal.writer().println("\u001B[1;34m" + heading + "\u001B[0m");
        terminal.flush();
    }

    // For tables and structured output
    public void printTable(String[] headers, String[][] data) {
        // Calculate column widths
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }

        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                colWidths[i] = Math.max(colWidths[i], row[i].length());
            }
        }

        // Print headers
        StringBuilder headerLine = new StringBuilder();
        StringBuilder dividerLine = new StringBuilder();

        for (int i = 0; i < headers.length; i++) {
            headerLine.append(String.format("| %-" + colWidths[i] + "s ", headers[i]));
            dividerLine.append("+");
            for (int j = 0; j < colWidths[i] + 2; j++) {
                dividerLine.append("-");
            }
        }
        headerLine.append("|");
        dividerLine.append("+");

        terminal.writer().println(dividerLine.toString());
        terminal.writer().println(headerLine.toString());
        terminal.writer().println(dividerLine.toString());

        // Print data
        for (String[] row : data) {
            StringBuilder dataLine = new StringBuilder();
            for (int i = 0; i < row.length; i++) {
                dataLine.append(String.format("| %-" + colWidths[i] + "s ", row[i]));
            }
            dataLine.append("|");
            terminal.writer().println(dataLine.toString());
        }

        terminal.writer().println(dividerLine.toString());
        terminal.flush();
    }
}