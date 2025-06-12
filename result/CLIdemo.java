package com.mybank.tui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

/**
 * Sample application to show how jLine can be used.
 *
 * @author sandarenu
 *
 */
/**
 * Console client for 'Banking' example
 *
 * @author Sviatoslav Kalinichuk
 */
public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "report", "exit"};
    }

    public void run() {
        AnsiConsole.systemInstall(); // needed to support ansi on Windows cmd
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<Completer>();

        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nThis is all of your ")
                        .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                        .append(":");

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name\tFirst Name\tBalance");
                    System.out.println("---------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        System.out.println(Bank.getCustomer(i).getLastName() + "\t\t" + Bank.getCustomer(i).getFirstName() + "\t\t$" + Bank.getCustomer(i).getAccount(0).getBalance());
                    }
                } else {
                    System.out.println(ANSI_RED+"Your bank has no customers!"+ANSI_RESET);
                }

            } else if (line.indexOf("customer") != -1) {
                try {
                    int custNo = 0;
                    if (line.length() > 8) {
                        String strNum = line.split(" ")[1];
                        if (strNum != null) {
                            custNo = Integer.parseInt(strNum);
                        }
                    }                    
                    Customer cust = Bank.getCustomer(custNo);
                    String accType = cust.getAccount(0) instanceof CheckingAccount ? "Checkinh" : "Savings";
                    
                    AttributedStringBuilder a = new AttributedStringBuilder()
                            .append("\nThis is detailed information about customer #")
                            .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                            .append("!");

                    System.out.println(a.toAnsi());
                    
                    System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
                    System.out.println("-------------------------------------------------------");
                    System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t$" + cust.getAccount(0).getBalance());
                } catch (Exception e) {
                    System.out
                        .println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                }
            } else if ("report".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nCustomer Report")
                        .append(":", AttributedStyle.BOLD.foreground(AttributedStyle.RED));

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name       First Name      Account Type    Balance         Overdraft");
                    System.out.println("-------------------------------------------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        Customer cust = Bank.getCustomer(i);
                        for (int j = 0; j < cust.getNumberOfAccounts(); j++) {
                            String accType = cust.getAccount(j) instanceof CheckingAccount ? "Checking" : "Savings";
                            double rateOrOverdraft = cust.getAccount(j) instanceof CheckingAccount ? 
                                ((CheckingAccount)cust.getAccount(j)).getOverdraftAmount() :
                                ((SavingsAccount)cust.getAccount(j)).getInterestRate();
                            System.out.printf("%-15s %-15s %-15s $%10.2f %10.2f%n",
                                cust.getLastName(),
                                cust.getFirstName(),
                                accType,
                                cust.getAccount(j).getBalance(),
                                rateOrOverdraft);
                        }
                    }
                } else {
                    System.out.println(ANSI_RED+"Your bank has no customers!"+ANSI_RESET);
                }
            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            } else {
                System.out
                        .println(ANSI_RED + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printWelcomeMessage() {
        System.out
                .println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");

    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customers\t\t- Show list of customers");
        System.out.println("customer \'index\'\t- Show customer details");
        System.out.println("report\t\t\t- Show detailed customer report");
        System.out.println("exit\t\t\t- Exit the app");

    }

    private String readLine(LineReader reader, String promtMessage) {
        try {
            String line = reader.readLine(promtMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            // e.g. ^C
            return null;
        } catch (EndOfFileException e) {
            // e.g. ^D
            return null;
        }
    }

    private void readCustomerData(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip empty lines until we find the number of customers
            while ((line = reader.readLine()) != null && line.trim().isEmpty()) {}
            int numCustomers = Integer.parseInt(line.trim());
            
            for (int i = 0; i < numCustomers; i++) {
                // Skip empty lines until we find customer info
                while ((line = reader.readLine()) != null && line.trim().isEmpty()) {}
                String[] customerInfo = line.trim().split("\t");
                String firstName = customerInfo[0];
                String lastName = customerInfo[1];
                int numAccounts = Integer.parseInt(customerInfo[2]);
                
                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(i);
                
                for (int j = 0; j < numAccounts; j++) {
                    // Skip empty lines until we find account info
                    while ((line = reader.readLine()) != null && line.trim().isEmpty()) {}
                    String[] accountInfo = line.trim().split("\t");
                    String accountType = accountInfo[0];
                    double balance = Double.parseDouble(accountInfo[1]);
                    double rateOrOverdraft = Double.parseDouble(accountInfo[2]);
                    
                    if (accountType.equals("S")) {
                        customer.addAccount(new SavingsAccount(balance, rateOrOverdraft));
                    } else if (accountType.equals("C")) {
                        customer.addAccount(new CheckingAccount(balance, rateOrOverdraft));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading customer data: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.readCustomerData("test.dat");
        shell.run();
    }
}