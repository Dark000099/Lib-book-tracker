import java.time.LocalDate;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private boolean isBorrowed;
    private String borrowerName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private double fine;

    public Book(int id, String title, String author, String isbn) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isBorrowed = false;
        this.fine = 0.0;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public void calculateFine() {
        if (isBorrowed && dueDate != null && LocalDate.now().isAfter(dueDate)) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            fine = daysOverdue * 0.50; // $0.50 per day
        } else {
            fine = 0.0;
        }
    }

    @Override
    public String toString() {
        String status = isBorrowed ? 
            String.format("Borrowed by: %s | Due: %s | Fine: $%.2f", 
                         borrowerName, dueDate, fine) :
            "Available";
        return String.format("ID: %d | Title: %s | Author: %s | ISBN: %s | Status: %s", 
                           id, title, author, isbn, status);
    }
}


