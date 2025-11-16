import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LibraryBookTracker {
    private static List<Book> books = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static int nextId = 1;

    public static void main(String[] args) {
        System.out.println("=== Library Book Tracker ===");
        loadBooks();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { saveBooks(); } catch (Exception ignored) {}
        }));
        
        while (true) {
            System.out.println("\n1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. View Overdue Books");
            System.out.println("6. Calculate Fines");
            System.out.println("7. Search Book");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    viewAllBooks();
                    break;
                case 3:
                    borrowBook();
                    break;
                case 4:
                    returnBook();
                    break;
                case 5:
                    viewOverdueBooks();
                    break;
                case 6:
                    calculateAllFines();
                    break;
                case 7:
                    searchBook();
                    break;
                case 8:
                    saveBooks();
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void addBook() {
        System.out.print("Enter title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();
        
        Book book = new Book(nextId++, title, author, isbn);
        books.add(book);
        System.out.println("Book added successfully!");
        saveBooks();
    }

    private static void viewAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books found!");
            return;
        }
        System.out.println("\n=== All Books ===");
        for (Book book : books) {
            book.calculateFine();
            System.out.println(book);
        }
    }

    private static void borrowBook() {
        System.out.print("Enter book ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        Book book = findBookById(id);
        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        
        if (book.isBorrowed()) {
            System.out.println("Book is already borrowed!");
            return;
        }
        
        System.out.print("Enter borrower name: ");
        String borrowerName = scanner.nextLine();
        System.out.print("Enter number of days to borrow: ");
        int days = scanner.nextInt();
        
        book.setBorrowed(true);
        book.setBorrowerName(borrowerName);
        book.setBorrowDate(LocalDate.now());
        book.setDueDate(LocalDate.now().plusDays(days));
        
        System.out.println("Book borrowed successfully! Due date: " + book.getDueDate());
        saveBooks();
    }

    private static void returnBook() {
        System.out.print("Enter book ID: ");
        int id = scanner.nextInt();
        
        Book book = findBookById(id);
        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        
        if (!book.isBorrowed()) {
            System.out.println("Book is not borrowed!");
            return;
        }
        
        book.calculateFine();
        if (book.getFine() > 0) {
            System.out.printf("Fine amount: $%.2f\n", book.getFine());
        }
        
        book.setBorrowed(false);
        book.setBorrowerName(null);
        book.setBorrowDate(null);
        book.setDueDate(null);
        book.setFine(0.0);
        
        System.out.println("Book returned successfully!");
        saveBooks();
    }

    private static void viewOverdueBooks() {
        List<Book> overdue = new ArrayList<>();
        for (Book book : books) {
            if (book.isBorrowed() && book.getDueDate() != null) {
                book.calculateFine();
                if (book.getFine() > 0) {
                    overdue.add(book);
                }
            }
        }
        
        if (overdue.isEmpty()) {
            System.out.println("No overdue books!");
            return;
        }
        
        System.out.println("\n=== Overdue Books ===");
        for (Book book : overdue) {
            System.out.println(book);
        }
    }

    private static void calculateAllFines() {
        double totalFines = 0.0;
        for (Book book : books) {
            book.calculateFine();
            totalFines += book.getFine();
        }
        System.out.printf("Total fines collected: $%.2f\n", totalFines);
    }

    private static void searchBook() {
        System.out.println("Search by: 1. Title  2. Author  3. ISBN");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                System.out.print("Enter title: ");
                String title = scanner.nextLine();
                books.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .forEach(System.out::println);
                break;
            case 2:
                System.out.print("Enter author: ");
                String author = scanner.nextLine();
                books.stream()
                    .filter(b -> b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                    .forEach(System.out::println);
                break;
            case 3:
                System.out.print("Enter ISBN: ");
                String isbn = scanner.nextLine();
                books.stream()
                    .filter(b -> b.getIsbn().equals(isbn))
                    .forEach(System.out::println);
                break;
        }
    }

    private static Book findBookById(int id) {
        return books.stream()
            .filter(b -> b.getId() == id)
            .findFirst()
            .orElse(null);
    }

    private static void saveBooks() {
        File file = new File("books.csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Book b : books) {
                String borrower = b.getBorrowerName() == null ? "" : escape(b.getBorrowerName());
                String borrowDate = b.getBorrowDate() == null ? "" : b.getBorrowDate().toString();
                String dueDate = b.getDueDate() == null ? "" : b.getDueDate().toString();
                writer.printf("%d|%s|%s|%s|%b|%s|%s|%s|%f\n",
                    b.getId(), escape(b.getTitle()), escape(b.getAuthor()), escape(b.getIsbn()),
                    b.isBorrowed(), borrower, borrowDate, dueDate, b.getFine());
            }
        } catch (IOException e) {
            System.out.println("Error saving books: " + e.getMessage());
        }
    }

    private static void loadBooks() {
        File file = new File("books.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 9) {
                    int id = Integer.parseInt(p[0]);
                    String title = unescape(p[1]);
                    String author = unescape(p[2]);
                    String isbn = unescape(p[3]);
                    boolean borrowed = Boolean.parseBoolean(p[4]);
                    String borrowerName = p[5].isEmpty() ? null : unescape(p[5]);
                    LocalDate borrowDate = p[6].isEmpty() ? null : LocalDate.parse(p[6]);
                    LocalDate dueDate = p[7].isEmpty() ? null : LocalDate.parse(p[7]);
                    double fine = Double.parseDouble(p[8]);

                    Book b = new Book(id, title, author, isbn);
                    b.setBorrowed(borrowed);
                    b.setBorrowerName(borrowerName);
                    b.setBorrowDate(borrowDate);
                    b.setDueDate(dueDate);
                    b.setFine(fine);
                    books.add(b);
                    if (id >= nextId) nextId = id + 1;
                }
            }
            System.out.println("Loaded books from file.");
        } catch (IOException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String s) {
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (char c : s.toCharArray()) {
            if (esc) { out.append(c); esc = false; }
            else if (c == '\\') { esc = true; }
            else { out.append(c); }
        }
        return out.toString();
    }
}


