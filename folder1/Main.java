import java.util.regex.*;
import java.io.*;
import java.util.*;

class User {
    private String username;
    private String password;
    private Portfolio portfolio;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.portfolio = new Portfolio(username);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}

class Authentication {
    private Map<String, User> users;
    private static final String CREDENTIALS_FILE = "credentials.txt";

    public Authentication() {
        this.users = new HashMap<>();
        loadCredentials();
    }

    private void loadCredentials() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    addUser(parts[0], parts[1]);
                } else {
                    System.out.println("Invalid data format in credentials.txt: " + line + ". Skipping.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading credentials: " + e.getMessage());
        }
    }

    private void saveCredentials() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (User user : users.values()) {
                writer.write(user.getUsername() + " " + user.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving credentials: " + e.getMessage());
        }
    }

    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public boolean addUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        if (password.isEmpty() || !Pattern.compile("(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9\\s]).{8,}").matcher(password).matches()) {
            System.out.println("\nPassword must contain at least one number, one alphabet character, one special character, and be at least 8 characters long.");
            return false;
        }
        users.put(username, new User(username, password));
        saveCredentials();
        return true;
    }

    public User getUser(String username) {
        return users.get(username);
    }
}

class Portfolio {
    private String username;
    private List<Stock> stocks;
    private static final String PORTFOLIO_DIRECTORY = "portfolio_files/";
    private static final String PORTFOLIO_FILE_EXTENSION = ".txt";

    public Portfolio(String username) {
        this.username = username;
        this.stocks = new ArrayList<>();
        loadPortfolio();
    }

    private String getPortfolioFilePath() {
        return PORTFOLIO_DIRECTORY + username + PORTFOLIO_FILE_EXTENSION;
    }

    private void loadPortfolio() {
        String filePath = getPortfolioFilePath();
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs(); 
            }
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    String[] parts = line.split(";");
                    if (parts.length != 4) {
                        System.out.println("Invalid data format in portfolio file at line " + lineCount + ": " + line + ". Skipping.");
                        continue;
                    }
                    try {
                        String symbol = parts[0];
                        String name = parts[1];
                        double price = Double.parseDouble(parts[2]);
                        int quantity = Integer.parseInt(parts[3]);
                        stocks.add(new Stock(symbol, name, price, quantity));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing data in portfolio file at line " + lineCount + ": " + line + ". Skipping.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading portfolio: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Error creating portfolio file: " + e.getMessage());
        }
    }

    private void savePortfolio() {
        String filePath = getPortfolioFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Stock stock : stocks) {
                writer.write(stock.getSymbol() + ";" + stock.getName() + ";" + stock.getPrice() + ";" + stock.getQuantity());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    public void addStock(Stock stock) {
        this.stocks.add(stock);
        savePortfolio();
    }

    public boolean removeStock(String symbol, int quantityToRemove) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equals(symbol)) {
                if (quantityToRemove < stock.getQuantity()) {
                    stock.setQuantity(stock.getQuantity() - quantityToRemove);
                    savePortfolio();
                    return true;
                } else if (quantityToRemove == stock.getQuantity()) {
                    stocks.remove(stock);
                    savePortfolio();
                    return true;
                } else {
                    System.out.println("Cannot remove more quantity than available.");
                    return false;
                }
            }
        }
        return false;
    }
    

    public List<Stock> getStocks() {
        return stocks;
    }

    public double getTotalValue() {
        double totalValue = 10000;
        for (Stock stock : stocks) {
            totalValue += stock.getValue();
        }
        return totalValue;
    }

    public Stock getMostProfitableStock() {
        Stock mostProfitable = null;
        double maxProfit = Double.MIN_VALUE;
        for (Stock stock : stocks) {
            double profit = stock.getValue();
            if (profit > maxProfit) {
                maxProfit = profit;
                mostProfitable = stock;
            }
        }
        return mostProfitable;
    }

    public void sortBySymbol() {
        stocks.sort(Comparator.comparing(Stock::getSymbol));
    }

    public void sortByPrice() {
        stocks.sort(Comparator.comparing(Stock::getPrice));
    }

    public void sortByQuantity() {
        stocks.sort(Comparator.comparing(Stock::getQuantity));
    }

    public void displayPortfolio() {
        if (stocks.isEmpty()) {
            System.out.println("Portfolio is empty.");
        } else {
            System.out.println("\n\nPortfolio:");
            System.out.printf("%-10s %-20s %-10s %-10s %-10s%n", "Symbol", "Name", "Price", "Quantity", "Value");
            for (Stock stock : stocks) {
                System.out.printf("%-10s %-20s %-10.2f %-10d %-10.2f%n",
                        stock.getSymbol(), stock.getName(), stock.getPrice(), stock.getQuantity(), stock.getValue());
            }
            System.out.println("\nTotal Portfolio Value: $" + getTotalValue());
        }
    }
}

class PaymentSystem1 {
    Scanner sc = new Scanner(System.in);

    public PaymentSystem1(int i) {
    }

    void CashPayment(double amount) {
        System.out.println("\nPaid " + amount + " INR by cash.");
    }

    void Credit(double totalPrice) {
        boolean flag = true;
        while (flag) {
            System.out.print("\nEnter Card Number : ");
            String card_num = sc.next();
            if (card_num.length() == 4) {
                flag = false;
            } else {
                System.out.println("Payment failed!! Try Again");
                flag = true;
            }
        }
        sc.nextLine();
        System.out.print("Enter Card Holder Name : ");
        sc.nextLine();
        System.out.print("Enter Expiry Month and Year (MM/YY) : ");
        sc.next();
        boolean flag1 = true;
        int temp = 0;
        while (flag1) {
            System.out.print("Enter CVV : ");
            String cvv = sc.next();
            if (cvv.length() == 3) {
                flag1 = false;
                System.out.println("Paid " + totalPrice + " INR by Credit card.");
            } else {
                System.out.println("Enter correct CVV!!");
                temp++;
                if (temp == 3) {
                    System.out.println("\nPayment Failed!!");
                    System.out.println("Card Blocked for 24 hours!!");
                    flag1 = false;
                }
            }
        }
    }

    void Debit(double totalPrice) {
        boolean flag = true;
        while (flag) {
            System.out.print("\nEnter Card Number : ");
            String card_num = sc.next();
            if (card_num.length() == 6) {
                flag = false;
            } else {
                System.out.println("\nPayment failed!! Try Again");
                flag = true;
            }
        }
        sc.nextLine();
        System.out.print("Enter Card Holder Name : ");
        sc.nextLine();
        System.out.print("Enter Expiry Month and Year (MM/YY) : ");
        sc.next();
        boolean flag1 = true;
        int temp = 0;
        while (flag1) {
            System.out.print("Enter CVV : ");
            String cvv = sc.next();
            if (cvv.length() == 3) {
                flag1 = false;
                System.out.println("Paid " + totalPrice + " INR by Debit card.");
            } else {
                System.out.println("Enter correct CVV!!");
                temp++;
                if (temp == 3) {
                    System.out.println("\nPayment Failed!!");
                    System.out.println("Card Blocked for 24 hours!!");
                    flag1 = false;
                }
            }
        }
    }

    void UPI(double totalPrice) {
        System.out.print("\nEnter UPI Id: ");
        sc.next();
        System.out.print("Enter UPI pin: ");
        sc.nextInt();
        System.out.println("Paid " + totalPrice + " INR by UPI.");
    }
}

class Stock {
    private String symbol;
    private String name;
    private double price;
    private int quantity;

    public Stock(String symbol, String name, double price, int quantity) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getValue() {
        return price * quantity;
    }
}

class StockMarket {
    private Portfolio portfolio;
    private Scanner scanner;
    public Map<String, Stock> availableStocks;
    private PaymentSystem1 paymentSystem;
    public Authentication auth;

    public StockMarket(Scanner scanner, User user) {
        this.portfolio = user.getPortfolio();
        this.scanner = scanner;
        this.availableStocks = new HashMap<>();
        initializeAvailableStocks();
        this.paymentSystem = new PaymentSystem1(0);
        this.auth = new Authentication();
    }

    private void initializeAvailableStocks() {
        availableStocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 135.00, 100));
        availableStocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 2350.00, 50));
        availableStocks.put("MSFT", new Stock("MSFT", "Microsoft Corporation", 300.00, 75));
        availableStocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 3300.00, 30));
        availableStocks.put("FB", new Stock("FB", "Meta Platforms Inc.", 330.00, 80));
        availableStocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 700.00, 60));
        availableStocks.put("NFLX", new Stock("NFLX", "Netflix Inc.", 520.00, 45));
        availableStocks.put("NVDA", new Stock("NVDA", "NVIDIA Corporation", 700.00, 55));
    }

    public void displayMenu() {
        System.out.println("\n\u001B[34m╔══════════════════════════════════════╗");
        System.out.println("║\u001B[38;5;208m              Main Menu               \u001B[34m║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║\u001B[38;5;208m [1] Buy Stock                        \u001B[34m║");
        System.out.println("║\u001B[38;5;208m [2] Sell Stock                       \u001B[34m║");
        System.out.println("║\u001B[38;5;208m [3] View Portfolio                   \u001B[34m║");
        System.out.println("║\u001B[38;5;208m [4] Display Most Profitable Share    \u001B[34m║");
        System.out.println("║\u001B[38;5;208m [5] Sort Portfolio                   \u001B[34m║");
        System.out.println("║\u001B[38;5;208m [6] Add New Stock                    \u001B[34m║");
        System.out.println("║\u001B[38;5;208m [7] Sign Out                         \u001B[34m║");
        System.out.println("╚══════════════════════════════════════╝\u001B[0m");
        System.out.print("Enter your choice: ");
    }

    public void processChoice(int choice) {
        switch (choice) {
            case 1:
                buyStock();
                break;
            case 2:
                sellStock();
                break;
            case 3:
                displayPortfolio();
                break;
            case 4:
                displayMostProfitableShare();
                break;
            case 5:
                sortPortfolio();
                break;
            case 6:
                addNewStock();
                break;
            case 7:
                System.out.println("\nSigning out...");
                return;
            default:
                System.out.println("\nInvalid choice. Please try again.");
        }
    }

    public void addNewStock() {
        boolean a = true;
        while (a) {
            System.out.print("Enter symbol: ");
            String symbol = scanner.nextLine();
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            System.out.print("Enter price: ");
            double price = scanner.nextDouble();
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            availableStocks.put(symbol, new Stock(symbol, name, price, quantity));
            System.out.println("\nNew stock added successfully.");
            a=false;
        }
    }

    public void buyStock() {
        displayCompanyMenu();
        System.out.println("\nEnter symbol and quantity to buy separated by a space:");
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid input. Please try again.\n");
            return;
        }
        String symbol = parts[0];
        int quantity = Integer.parseInt(parts[1]);
        Stock stockToBuy = availableStocks.get(symbol);
        if (stockToBuy == null) {
            System.out.println("\nStock not found.\n");
            return;
        }
        double totalPrice = stockToBuy.getPrice() * quantity;
        double availableFunds = portfolio.getTotalValue();
        if (totalPrice > availableFunds) {
            System.out.println("\nInsufficient funds to buy.\n");
            return;
        }
        portfolio.addStock(new Stock(stockToBuy.getSymbol(), stockToBuy.getName(), stockToBuy.getPrice(), quantity));
        System.out.println("\nBought " + quantity + " shares of " + stockToBuy.getName() + " (" + symbol + ") at $" + stockToBuy.getPrice() + " each.");

        System.out.println("\nChoose payment method for " + totalPrice + " INR:");
        System.out.println("1. Cash Payment");
        System.out.println("2. Credit Card Payment");
        System.out.println("3. Debit Card Payment");
        System.out.println("4. UPI Payment");
        System.out.print("Enter your choice: ");
        int paymentChoice = scanner.nextInt();
        scanner.nextLine();
        switch (paymentChoice) {
            case 1:
                paymentSystem.CashPayment(totalPrice);
                break;
            case 2:
                paymentSystem.Credit(totalPrice);
                break;
            case 3:
                paymentSystem.Debit(totalPrice);
                break;
            case 4:
                paymentSystem.UPI(totalPrice);
                break;
            default:
                System.out.println("\nInvalid choice! Payment failed.");
        }
    }

    public void sellStock() {
        portfolio.displayPortfolio();
        System.out.println("\nEnter symbol and quantity to sell separated by a space:");
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        if (parts.length != 2) {
            System.out.println("\nInvalid input. Please try again.");
            return;
        }
        String symbol = parts[0];
        int quantity = Integer.parseInt(parts[1]);
        Stock stockToSell = portfolio.getStocks().stream()
                .filter(stock -> stock.getSymbol().equals(symbol))
                .findFirst().orElse(null);
        if (stockToSell == null) {
            System.out.println("\nStock not found in portfolio.");
            return;
        }
        if (quantity > stockToSell.getQuantity()) {
            System.out.println("\nInsufficient quantity to sell.");
            return;
        }
        double totalPrice = stockToSell.getPrice() * quantity;
        if (portfolio.removeStock(symbol, quantity)) {
            System.out.println("\nSold " + quantity + " shares of " + stockToSell.getName() + " (" + symbol + ") at $" + stockToSell.getPrice() + " each.");
            System.out.println("Total amount received: $" + totalPrice);
        } else {
            System.out.println("\nFailed to sell shares.");
        }
    }


    public void displayPortfolio() {
        portfolio.displayPortfolio();
    }

    private void displayCompanyMenu() {
        System.out.println("\nAvailable Stocks:");
        System.out.printf("%-10s %-20s %-10s %-10s%n", "Symbol", "Name", "Price", "Quantity");
        for (Stock stock : availableStocks.values()) {
            System.out.printf("%-10s %-20s %-10.2f %-10d%n",
                    stock.getSymbol(), stock.getName(), stock.getPrice(), stock.getQuantity());
        }
    }

    public void displayMostProfitableShare() {
        Stock mostProfitableStock = portfolio.getMostProfitableStock();
        if (mostProfitableStock != null) {
            System.out.println("\nMost Profitable Share:");
            System.out.println("Symbol: " + mostProfitableStock.getSymbol());
            System.out.println("Name: " + mostProfitableStock.getName());
            System.out.println("Price: " + mostProfitableStock.getPrice());
            System.out.println("Quantity: " + mostProfitableStock.getQuantity());
            System.out.println("Value: " + mostProfitableStock.getValue());
        } else {
            System.out.println("\nNo shares in the portfolio.");
        }
    }

    public void sortPortfolio() {
        System.out.println("\n\nSort Portfolio By:");
        System.out.println("1. Symbol");
        System.out.println("2. Price");
        System.out.println("3. Quantity");
        System.out.print("Enter your choice: ");
        int sortChoice = scanner.nextInt();
        scanner.nextLine();
        switch (sortChoice) {
            case 1:
                portfolio.sortBySymbol();
                break;
            case 2:
                portfolio.sortByPrice();
                break;
            case 3:
                portfolio.sortByQuantity();
                break;
            default:
                System.out.println("\nInvalid choice!");
                return;
        }
        System.out.println("\nPortfolio sorted successfully.");
        displayPortfolio();
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWelcome to the Stock Market System");
        Authentication auth = new Authentication();
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\n\n1. Sign In");
            System.out.println("2. Sign Up");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    signIn(scanner, auth);
                    break;
                case 2:
                    signUp(scanner, auth);
                    break;
                case 3:
                    System.out.println("\nExiting...");
                    scanner.close();
                    isRunning = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    public static void signIn(Scanner scanner, Authentication auth) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (auth.authenticate(username, password)) {
            System.out.println("Sign in successful!");
            User user = auth.getUser(username);
            StockMarket stockMarket = new StockMarket(scanner, user);
            boolean isLoggedIn = true;
            while (isLoggedIn) {
                stockMarket.displayMenu();
                int choice = scanner.nextInt();
                scanner.nextLine();
                stockMarket.processChoice(choice);
                if (choice == 7) {
                    isLoggedIn = false;
                }
            }
        } else {
            System.out.println("\nInvalid username or password. Please try again.");
        }
    }

    public static void signUp(Scanner scanner, Authentication auth) {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (auth.addUser(username, password)) {
            System.out.println("\nSign up successful! You can now sign in.");
        } else {
            System.out.println("\nUsername already exists or password does not meet requirements. Please try again.");
        }
    }
   
}
