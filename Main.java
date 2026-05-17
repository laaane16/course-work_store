import java.io.*;
import java.time.LocalDate;
import java.util.Scanner;

// Ошибки
class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String msg) {
        super(msg);
    }
}

class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String msg) {
        super(msg);
    }
}

class OrderAlreadyExistsException extends RuntimeException {
    public OrderAlreadyExistsException(String msg) {
        super(msg);
    }
}

class OrderRemoveException extends RuntimeException {
    public OrderRemoveException(String msg) {
        super(msg);
    }
}

// Интернет-магазин (агрегатор)
class Store {
    private OrdersList ordersList;
    private ProductList catalog;

    public Store() {
        ordersList = new OrdersList();
        catalog = new ProductList();
    }

    public void addProductToCatalog(Product product) {
        catalog.addProduct(product);
    }

    public ProductList getCatalog() {
        return catalog;
    }

    public void setCatalog(ProductList catalog) {
        this.catalog = catalog;
    }

    public void printCatalog() {
        System.out.println("Каталог:");
        catalog.printProducts();
    }

    public Product getProduct(String name) {
        return catalog.findProduct(name);
    }

    public OrdersList getOrdersList() {
        return ordersList;
    }

    public void setOrdersList(OrdersList ordersList) {
        this.ordersList = ordersList;
    }

    public void createOrder(String surname, String productName) {

        if (ordersList.findOrder(surname) != null) {
            throw new OrderAlreadyExistsException(
                    "Заказ уже существует для фамилии: " + surname
            );
        }

        Product product = getProduct(productName);

        if (product == null)
            throw new ProductNotFoundException("Товар не найден: " + productName);

        ordersList.addOrder(new Order(surname, LocalDate.now(), product));
    }

    public void addProductToOrder(String surname, String productName) {
        Order order = ordersList.findOrder(surname);

        if (order == null)
            throw new OrderNotFoundException("Заказ не найден: " + surname);

        Product product = getProduct(productName);

        if (product == null)
            throw new ProductNotFoundException("Товар не найден: " + productName);

        order.addProduct(product);
    }

    public void removeProductFromOrder(String surname, String productName) {
        Order order = ordersList.findOrder(surname);

        if (order == null)
            throw new OrderNotFoundException("Заказ не найден: " + surname);

        order.removeProduct(productName);

        if (order.getProducts().isEmpty()) {
            ordersList.removeOrder(surname);
            System.out.println("Заказ удалён (был пуст)");
        }
    }

    public void removeOrder(String surname) {
        boolean removed = ordersList.removeOrder(surname);

        if (!removed) {
            throw new OrderNotFoundException("Нельзя удалить: заказ не найден для " + surname);
        }
    }

    public Order findOrder(String surname) {
        return ordersList.findOrder(surname);
    }

    public double calculateCost() {
        return ordersList.calculateCost();
    }

    public void printAll() {
        if (ordersList.getSize() == 0) {
            System.out.println("Нет заказов.");
            return;
        }

        for (int i = 0; i < ordersList.getSize(); i++) {
            ordersList.getOrderByIndex(i).printOrder();
            System.out.println("------------------");
        }
    }
}

// Список заказов
class OrdersList {
    private Order[] orders = new Order[10];
    private int size;

    private void resize() {
        Order[] newArr = new Order[orders.length * 2];
        System.arraycopy(orders, 0, newArr, 0, size);
        orders = newArr;
    }

    // TODO
    public void addOrder(Order order) {
        if (size == orders.length) resize();

        int i = 0;

        // ищем позицию (по возрастанию даты)
        while (i < size && !orders[i].getDate().isAfter(order.getDate())) {
            i++;
        }

        // сдвиг вправо
        for (int j = size; j > i; j--) {
            orders[j] = orders[j - 1];
        }

        orders[i] = order;
        size++;
    }

    public boolean removeOrder(String surname) {
        for (int i = 0; i < size; i++) {
            if (orders[i].getCustomerSurname().equals(surname)) {

                for (int j = i; j < size - 1; j++) {
                    orders[j] = orders[j + 1];
                }

                size--;
                return true;
            }
        }
        return false;
    }

    public Order findOrder(String surname) {
        for (int i = 0; i < size; i++) {
            if (orders[i].getCustomerSurname().equals(surname)) {
                return orders[i];
            }
        }
        return null;
    }

    public double calculateCost() {
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += orders[i].calculateCost();
        }
        return sum;
    }

    public int getSize() {
        return size;
    }

    public Order getOrderByIndex(int i) {
        return orders[i];
    }
}

// Заказ
class Order {
    private String surname;
    private LocalDate date;
    private ProductList products;

    public Order(String surname, LocalDate date, Product firstProduct) {
        this.surname = surname;
        this.date = date;
        this.products = new ProductList();
        this.products.addProduct(firstProduct);
    }

    public String getCustomerSurname() {
        return surname;
    }

    public LocalDate getDate() {
        return date;
    }

    public ProductList getProducts() {
        return products;
    }

    public void addProduct(Product p) {
        products.addProduct(p);
    }

    public void removeProduct(String name) {
        products.removeProduct(name);
    }

    public double calculateCost() {
        return products.calculateCost();
    }

    public void printOrder() {
        System.out.println("Фамилия: " + surname);
        System.out.println("Дата: " + date);
        products.printProducts();
        System.out.println("Сумма: " + calculateCost());
    }
}

// Список товаров
class ProductList {
    private ProductNode head;
    private ProductNode tail;

    public ProductNode getHead() {
        return head;
    }

    public void addProduct(Product p) {
        if (head == null) {
            head = tail = new ProductNode(p);
        } else {
            insertRelative(tail, p, true); // вставка после хвоста
        }
    }

    public void insertRelative(ProductNode target, Product newProduct, boolean insertAfter) {
        if (target == null) {
            throw new IllegalArgumentException("Опорный узел не может быть null");
        }

        ProductNode newNode = new ProductNode(newProduct);

        if (insertAfter) {
            newNode.setPrev(target);
            newNode.setNext(target.getNext());

            if (target.getNext() != null) {
                target.getNext().setPrev(newNode);
            } else {
                tail = newNode; // обновляем хвост
            }

            target.setNext(newNode);

        } else {
            newNode.setNext(target);
            newNode.setPrev(target.getPrev());

            if (target.getPrev() != null) {
                target.getPrev().setNext(newNode);
            } else {
                head = newNode; // обновляем голову
            }

            target.setPrev(newNode);
        }
    }

    public void removeProduct(String name) {
        ProductNode cur = head;

        while (cur != null) {
            if (cur.getData().getName().equals(name)) {

                if (cur == head) {
                    head = cur.getNext();
                    if (head != null) head.setPrev(null);

                } else if (cur == tail) {
                    tail = cur.getPrev();
                    if (tail != null) tail.setNext(null);

                } else {
                    cur.getPrev().setNext(cur.getNext());
                    cur.getNext().setPrev(cur.getPrev());
                }

                return;
            }
            cur = cur.getNext();
        }
    }

    public Product findProduct(String name) {
        ProductNode cur = head;

        while (cur != null) {
            if (cur.getData().getName().equals(name))
                return cur.getData();
            cur = cur.getNext();
        }
        return null;
    }

    public double calculateCost() {
        double sum = 0;
        ProductNode cur = head;

        while (cur != null) {
            sum += cur.getData().getPrice();
            cur = cur.getNext();
        }
        return sum;
    }

    public void printProducts() {
        ProductNode cur = head;

        while (cur != null) {
            System.out.println("- " + cur.getData().getName() +
                    " (" + cur.getData().getPrice() + ")");
            cur = cur.getNext();
        }
    }

    public boolean isEmpty() {
        return head == null;
    }
}

// Узел товаров
class ProductNode {
    private Product data;
    private ProductNode next, prev;

    public ProductNode(Product data) {
        this.data = data;
    }

    public Product getData() {
        return data;
    }

    public ProductNode getNext() {
        return next;
    }

    public ProductNode getPrev() {
        return prev;
    }

    public void setNext(ProductNode n) {
        next = n;
    }

    public void setPrev(ProductNode p) {
        prev = p;
    }
}

// Товар
class Product {
    private String name;
    private double price;

    public Product(String n, double p) {
        name = n;
        price = p;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

// Исполняемый клас
public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static Store store = new Store();

    public static void main(String[] args) {

        String catalogFile = readValidFile("Файл каталога");
        String ordersFile = readValidFile("Файл заказов");

        store.setCatalog(loadCatalog(catalogFile));
        store.setOrdersList(loadOrders(ordersFile));

        while (true) {
            menu();
            int c = scanner.nextInt();
            scanner.nextLine();

            switch (c) {
                case 1 -> addOrder();
                case 2 -> removeOrder();
                case 3 -> findOrder();
                case 4 -> addProduct();
                case 5 -> removeProduct();
                case 6 -> store.printAll();
                case 7 -> System.out.println(store.calculateCost());
                case 8 -> store.printCatalog();

                case 9 -> {
                    saveCatalog(store.getCatalog(), catalogFile);
                    saveOrders(store.getOrdersList(), ordersFile);
                    System.out.println("Сохранено!");
                }

                case 0 -> {
                    saveCatalog(store.getCatalog(), catalogFile);
                    saveOrders(store.getOrdersList(), ordersFile);
                    return;
                }
            }
        }
    }

    private static String readValidFile(String title) {
        while (true) {
            System.out.print(title + ": ");
            String file = scanner.nextLine();

            File f = new File(file);

            if (!f.exists() || !f.isFile()) {
                System.out.println("❌ Файл не найден. Попробуйте снова.");
                continue;
            }

            if (!f.canRead()) {
                System.out.println("❌ Нет прав на чтение файла. Попробуйте снова.");
                continue;
            }

            return file;
        }
    }

    private static void addOrder() {
        try {
            System.out.print("Фамилия: ");
            String s = scanner.nextLine();

            store.printCatalog();

            System.out.print("Товар: ");
            String p = scanner.nextLine();

            store.createOrder(s, p);

            System.out.println("OK");

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addProduct() {
        try {
            System.out.print("Фамилия: ");
            String s = scanner.nextLine();

            store.printCatalog();

            System.out.print("Товар: ");
            String p = scanner.nextLine();

            store.addProductToOrder(s, p);

            System.out.println("Добавлено");

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void removeProduct() {
        try {
            System.out.print("Фамилия: ");
            String s = scanner.nextLine();

            System.out.print("Товар: ");
            String p = scanner.nextLine();

            store.removeProductFromOrder(s, p);

            System.out.println("Готово");

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void findOrder() {
        try {
            System.out.print("Фамилия: ");
            Order o = store.findOrder(scanner.nextLine());

            if (o == null)
                throw new OrderNotFoundException("Заказ не найден");

            o.printOrder();

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void removeOrder() {
        try {
            System.out.print("Фамилия: ");
            String s = scanner.nextLine();

            store.removeOrder(s);

            System.out.println("Заказ удалён");

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void menu() {
        System.out.println("\n1. Создать заказ");
        System.out.println("2. Удалить заказ");
        System.out.println("3. Найти заказ");
        System.out.println("4. Добавить товар");
        System.out.println("5. Удалить товар");
        System.out.println("6. Показать все заказы");
        System.out.println("7. Общая стоимость");
        System.out.println("8. Каталог");
        System.out.println("9. Сохранить");
        System.out.println("0. Выход");
    }

    private static ProductList loadCatalog(String file) {
        ProductList catalog = new ProductList();

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(";");
                catalog.addProduct(new Product(p[0], Double.parseDouble(p[1])));
            }
        } catch (IOException e) {
            System.out.println("Ошибка каталога: " + e.getMessage());
        }

        return catalog;
    }

    private static OrdersList loadOrders(String file) {
        OrdersList orders = new OrdersList();

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = r.readLine()) != null) {

                String[] parts = line.split(";");

                String surname = parts[0];
                LocalDate date = LocalDate.parse(parts[1]);

                String[] first = parts[2].split(",");
                Product firstProduct = new Product(first[0], Double.parseDouble(first[1]));

                Order order = new Order(surname, date, firstProduct);

                for (int i = 3; i < parts.length; i++) {
                    String[] p = parts[i].split(",");
                    order.addProduct(new Product(p[0], Double.parseDouble(p[1])));
                }

                orders.addOrder(order);
            }

        } catch (IOException e) {
            System.out.println("Ошибка заказов: " + e.getMessage());
        }

        return orders;
    }

    private static void saveCatalog(ProductList catalog, String file) {
        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {

            ProductNode cur = catalog.getHead();

            while (cur != null) {
                Product p = cur.getData();
                w.println(p.getName() + ";" + p.getPrice());
                cur = cur.getNext();
            }

        } catch (IOException e) {
            System.out.println("Ошибка сохранения каталога: " + e.getMessage());
        }
    }

    private static void saveOrders(OrdersList orders, String file) {
        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {

            for (int i = 0; i < orders.getSize(); i++) {

                Order o = orders.getOrderByIndex(i);

                StringBuilder sb = new StringBuilder();
                sb.append(o.getCustomerSurname())
                        .append(";")
                        .append(o.getDate());

                ProductNode cur = o.getProducts().getHead();

                while (cur != null) {
                    Product p = cur.getData();
                    sb.append(";")
                            .append(p.getName())
                            .append(",")
                            .append(p.getPrice());

                    cur = cur.getNext();
                }

                w.println(sb);
            }

        } catch (IOException e) {
            System.out.println("Ошибка сохранения заказов: " + e.getMessage());
        }
    }
}