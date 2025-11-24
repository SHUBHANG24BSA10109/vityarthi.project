package PROJECT;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class LibraryManagementSwingApp extends JFrame {


    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private int bookIdCounter = 1;
    private int memberIdCounter = 1;
    private int transactionIdCounter = 1;


    private DefaultTableModel bookTableModel;
    private JTable bookTable;
    private JTextField titleField, authorField, categoryField, quantityField, bookSearchField;


    private DefaultTableModel memberTableModel;
    private JTable memberTable;
    private JTextField memberNameField, memberEmailField, memberPhoneField;

    private DefaultTableModel loanTableModel;
    private JTable loanTable;
    private JComboBox<Book> bookComboBox;
    private JComboBox<Member> memberComboBox;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSwingApp app = new LibraryManagementSwingApp();
            app.setVisible(true);
        });
    }

    public LibraryManagementSwingApp() {
        setTitle("Library Book Management System - Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        seedSampleData();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", createBooksPanel());
        tabs.addTab("Members", createMembersPanel());
        tabs.addTab("Loans", createLoansPanel());

        add(tabs, BorderLayout.CENTER);
    }


    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top form
        JPanel form = new JPanel(new GridLayout(2, 5, 5, 5));
        titleField = new JTextField();
        authorField = new JTextField();
        categoryField = new JTextField();
        quantityField = new JTextField();
        JButton addBookBtn = new JButton("Add Book");
        JButton updateBookBtn = new JButton("Update Selected");

        form.setBorder(BorderFactory.createTitledBorder("Book Details"));
        form.add(new JLabel("Title:"));
        form.add(new JLabel("Author:"));
        form.add(new JLabel("Category:"));
        form.add(new JLabel("Quantity:"));
        form.add(new JLabel("")); // empty

        form.add(titleField);
        form.add(authorField);
        form.add(categoryField);
        form.add(quantityField);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBookBtn);
        btnPanel.add(updateBookBtn);
        form.add(btnPanel);

        panel.add(form, BorderLayout.NORTH);


        String[] cols = {"ID", "Title", "Author", "Category", "Total", "Available"};
        bookTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(bookTableModel);
        JScrollPane scroll = new JScrollPane(bookTable);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom search + delete
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookSearchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        JButton deleteBtn = new JButton("Delete Selected");
        bottom.add(new JLabel("Search by title:"));
        bottom.add(bookSearchField);
        bottom.add(searchBtn);
        bottom.add(showAllBtn);
        bottom.add(deleteBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        addBookBtn.addActionListener(_ -> addBookFromForm());
        updateBookBtn.addActionListener(_ -> updateSelectedBook());
        deleteBtn.addActionListener(_ -> deleteSelectedBook());
        searchBtn.addActionListener(_ -> searchBooks());
        showAllBtn.addActionListener(_ -> refreshBookTable());

        refreshBookTable();
        return panel;
    }

    private void addBookFromForm() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String category = categoryField.getText().trim();
        String qtyText = quantityField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || category.isEmpty() || qtyText.isEmpty()) {
            showMessage("Please fill all fields for book.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException ex) {
            showMessage("Quantity must be a number.");
            return;
        }
        if (qty <= 0) {
            showMessage("Quantity must be positive.");
            return;
        }

        Book book = new Book(bookIdCounter++, title, author, category, qty, qty);
        books.add(book);
        clearBookForm();
        refreshBookTable();
        refreshBookComboBox();
        showMessage("Book added successfully.");
    }

    private void updateSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a book to update.");
            return;
        }
        int id = (int) bookTableModel.getValueAt(row, 0);
        Book book = findBookById(id);
        if (book == null) {
            showMessage("Selected book not found.");
            return;
        }

        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String category = categoryField.getText().trim();
        String qtyText = quantityField.getText().trim();

        if (!title.isEmpty()) book.setTitle(title);
        if (!author.isEmpty()) book.setAuthor(author);
        if (!category.isEmpty()) book.setCategory(category);
        if (!qtyText.isEmpty()) {
            try {
                int newTotal = Integer.parseInt(qtyText);
                if (newTotal >= 0) {
                    int diff = newTotal - book.getTotalCopies();
                    int newAvail = book.getAvailableCopies() + diff;
                    if (newAvail >= 0) {
                        book.setTotalCopies(newTotal);
                        book.setAvailableCopies(newAvail);
                    } else {
                        showMessage("Cannot reduce total below already issued books.");
                    }
                }
            } catch (NumberFormatException ex) {
                showMessage("Invalid quantity. Skipping quantity update.");
            }
        }
        refreshBookTable();
        refreshBookComboBox();
        showMessage("Book updated.");
    }

    private void deleteSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a book to delete.");
            return;
        }
        int id = (int) bookTableModel.getValueAt(row, 0);
        Book book = findBookById(id);
        if (book == null) return;

        for (Transaction t : transactions) {
            if (t.getBookId() == id && !t.isReturned()) {
                showMessage("Cannot delete. Book is currently issued.");
                return;
            }
        }
        books.remove(book);
        refreshBookTable();
        refreshBookComboBox();
        showMessage("Book deleted.");
    }

    private void searchBooks() {
        String keyword = bookSearchField.getText().trim().toLowerCase();
        bookTableModel.setRowCount(0);
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword)) {
                bookTableModel.addRow(new Object[]{
                        b.getId(), b.getTitle(), b.getAuthor(),
                        b.getCategory(), b.getTotalCopies(), b.getAvailableCopies()
                });
            }
        }
    }

    private void refreshBookTable() {
        bookTableModel.setRowCount(0);
        for (Book b : books) {
            bookTableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(),
                    b.getCategory(), b.getTotalCopies(), b.getAvailableCopies()
            });
        }
    }

    private void clearBookForm() {
        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        quantityField.setText("");
    }


    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 4, 5, 5));
        memberNameField = new JTextField();
        memberEmailField = new JTextField();
        memberPhoneField = new JTextField();
        JButton addMemberBtn = new JButton("Add Member");
        JButton updateMemberBtn = new JButton("Update Selected");

        form.setBorder(BorderFactory.createTitledBorder("Member Details"));
        form.add(new JLabel("Name:"));
        form.add(new JLabel("Email:"));
        form.add(new JLabel("Phone:"));
        form.add(new JLabel(""));

        form.add(memberNameField);
        form.add(memberEmailField);
        form.add(memberPhoneField);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addMemberBtn);
        btnPanel.add(updateMemberBtn);
        form.add(btnPanel);

        panel.add(form, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Phone"};
        memberTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        memberTable = new JTable(memberTableModel);
        JScrollPane scroll = new JScrollPane(memberTable);
        panel.add(scroll, BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Selected");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(deleteBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        addMemberBtn.addActionListener(_ -> addMemberFromForm());
        updateMemberBtn.addActionListener(_ -> updateSelectedMember());
        deleteBtn.addActionListener(_ -> deleteSelectedMember());

        refreshMemberTable();
        return panel;
    }

    private void addMemberFromForm() {
        String name = memberNameField.getText().trim();
        String email = memberEmailField.getText().trim();
        String phone = memberPhoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showMessage("Please fill all fields for member.");
            return;
        }
        Member m = new Member(memberIdCounter++, name, email, phone);
        members.add(m);
        clearMemberForm();
        refreshMemberTable();
        refreshMemberComboBox();
        showMessage("Member added.");
    }

    private void updateSelectedMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a member to update.");
            return;
        }
        int id = (int) memberTableModel.getValueAt(row, 0);
        Member member = findMemberById(id);
        if (member == null) return;

        String name = memberNameField.getText().trim();
        String email = memberEmailField.getText().trim();
        String phone = memberPhoneField.getText().trim();

        if (!name.isEmpty()) member.setName(name);
        if (!email.isEmpty()) member.setEmail(email);
        if (!phone.isEmpty()) member.setPhone(phone);

        refreshMemberTable();
        refreshMemberComboBox();
        showMessage("Member updated.");
    }

    private void deleteSelectedMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a member to delete.");
            return;
        }
        int id = (int) memberTableModel.getValueAt(row, 0);
        Member member = findMemberById(id);
        if (member == null) return;

        for (Transaction t : transactions) {
            if (t.getMemberId() == id && !t.isReturned()) {
                showMessage("Cannot delete. Member has unreturned books.");
                return;
            }
        }
        members.remove(member);
        refreshMemberTable();
        refreshMemberComboBox();
        showMessage("Member deleted.");
    }

    private void refreshMemberTable() {
        memberTableModel.setRowCount(0);
        for (Member m : members) {
            memberTableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getEmail(), m.getPhone()
            });
        }
    }

    private void clearMemberForm() {
        memberNameField.setText("");
        memberEmailField.setText("");
        memberPhoneField.setText("");
    }

    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        bookComboBox = new JComboBox<>();
        memberComboBox = new JComboBox<>();
        JButton issueBtn = new JButton("Issue Book");

        form.setBorder(BorderFactory.createTitledBorder("Issue Book"));
        form.add(new JLabel("Select Book:"));
        form.add(new JLabel("Select Member:"));
        form.add(bookComboBox);
        form.add(memberComboBox);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(issueBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"TxnID", "Book", "Member", "Issue Date", "Return Date", "Status"};
        loanTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        loanTable = new JTable(loanTableModel);
        JScrollPane scroll = new JScrollPane(loanTable);
        panel.add(scroll, BorderLayout.CENTER);

        JButton returnBtn = new JButton("Mark Selected as Returned");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(returnBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        issueBtn.addActionListener(_ -> issueSelectedBook());
        returnBtn.addActionListener(_ -> returnSelectedTransaction());

        refreshBookComboBox();
        refreshMemberComboBox();
        refreshLoanTable();

        return panel;
    }

    private void issueSelectedBook() {
        Book book = (Book) bookComboBox.getSelectedItem();
        Member member = (Member) memberComboBox.getSelectedItem();

        if (book == null || member == null) {
            showMessage("Please select both book and member.");
            return;
        }
        if (book.getAvailableCopies() <= 0) {
            showMessage("No copies available for this book.");
            return;
        }

        Transaction t = new Transaction(
                transactionIdCounter++,
                book.getId(),
                member.getId(),
                LocalDate.now(),
                null,
                false
        );
        transactions.add(t);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        refreshBookTable();
        refreshBookComboBox();
        refreshLoanTable();
        showMessage("Book issued. Transaction ID: " + t.getId());
    }

    private void returnSelectedTransaction() {
        int row = loanTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a transaction.");
            return;
        }
        int txnId = (int) loanTableModel.getValueAt(row, 0);
        Transaction t = findTransactionById(txnId);
        if (t == null) return;

        if (t.isReturned()) {
            showMessage("This book is already returned.");
            return;
        }
        Book book = findBookById(t.getBookId());
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        t.setReturned(true);
        t.setReturnDate(LocalDate.now());
        refreshBookTable();
        refreshBookComboBox();
        refreshLoanTable();
        showMessage("Book returned.");
    }

    private void refreshLoanTable() {
        loanTableModel.setRowCount(0);
        for (Transaction t : transactions) {
            Book b = findBookById(t.getBookId());
            Member m = findMemberById(t.getMemberId());
            String bookTitle = (b != null) ? b.getTitle() : "Unknown";
            String memberName = (m != null) ? m.getName() : "Unknown";
            String status = t.isReturned() ? "Returned" : "Issued";
            loanTableModel.addRow(new Object[]{
                    t.getId(),
                    bookTitle,
                    memberName,
                    t.getIssueDate(),
                    t.getReturnDate(),
                    status
            });
        }
    }

    private void refreshBookComboBox() {
        bookComboBox.removeAllItems();
        for (Book b : books) {
            bookComboBox.addItem(b);
        }
    }

    private void refreshMemberComboBox() {
        memberComboBox.removeAllItems();
        for (Member m : members) {
            memberComboBox.addItem(m);
        }
    }



    private Book findBookById(int id) {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    private Member findMemberById(int id) {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    private Transaction findTransactionById(int id) {
        for (Transaction t : transactions) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void seedSampleData() {
        // Sample books
        books.add(new Book(bookIdCounter++, "Java Programming", "Herbert Schildt", "Programming", 5, 5));
        books.add(new Book(bookIdCounter++, "Data Structures", "Narasimha Karumanchi", "CS", 3, 3));
        // Sample members
        members.add(new Member(memberIdCounter++, "Rahul Sharma", "rahul@example.com", "9876543210"));
        members.add(new Member(memberIdCounter++, "Sneha Patel", "sneha@example.com", "9123456780"));
    }
}



class Book {
    private final int id;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, String category, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public int getTotalCopies() { return totalCopies; }

    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }

    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    @Override
    public String toString() {
        // ComboBox ke liye
        return id + " - " + title;
    }
}

class Member {
    private final int id;
    private String name;
    private String email;
    private String phone;

    public Member(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

class Transaction {
    private final int id;
    private final int bookId;
    private final int memberId;
    private final LocalDate issueDate;
    private LocalDate returnDate;
    private boolean returned;

    public Transaction(int id, int bookId, int memberId, LocalDate issueDate,
                       LocalDate returnDate, boolean returned) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
        this.returned = returned;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }
}