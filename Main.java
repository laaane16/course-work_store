import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.*;
import java.time.LocalDate;

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

// Интернет-магазин (агрегатор)
class Store {
    private OrdersList ordersList = new OrdersList();
    private ProductList catalog = new ProductList();

    public ProductList getCatalog() {
        return catalog;
    }

    public OrdersList getOrdersList() {
        return ordersList;
    }

    public void setCatalog(ProductList catalog) {
        this.catalog = catalog;
    }

    public void setOrdersList(OrdersList ordersList) {
        this.ordersList = ordersList;
    }

    public Product getProduct(String name) {
        return catalog.findProduct(name);
    }

    public void createOrder(String surname, String productName) {

        if (ordersList.findOrder(surname) != null)
            throw new OrderAlreadyExistsException("Заказ уже существует");

        Product p = getProduct(productName);

        if (p == null)
            throw new ProductNotFoundException("Товар не найден");

        ordersList.addOrder(
                new Order(surname, LocalDate.now(), p)
        );
    }

    public void addProductToOrder(String surname, String productName) {
        Order order = ordersList.findOrder(surname);

        if (order == null)
            throw new OrderNotFoundException("Заказ не найден");

        Product p = getProduct(productName);

        if (p == null)
            throw new ProductNotFoundException("Товар не найден");

        order.addProduct(p);
    }

    public void removeProductFromOrder(String surname, String productName) {
        Order order = ordersList.findOrder(surname);

        if (order == null)
            throw new OrderNotFoundException("Заказ не найден");

        order.removeProduct(productName);

        if (order.getProducts().isEmpty()) {
            ordersList.removeOrder(surname);
        }
    }

    public void removeOrder(String surname) {
        if (!ordersList.removeOrder(surname))
            throw new OrderNotFoundException("Заказ не найден");
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
        products = new ProductList();
        products.addProduct(firstProduct);
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

    public int getProductsCount() {

        int count = 0;

        ProductNode cur = products.getHead();

        while (cur != null) {
            count++;
            cur = cur.getNext();
        }

        return count;
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

public class Main extends Application {

    private Store store = new Store();

    private TableView<Order> ordersTable = new TableView<>();
    private TableView<Product> productsTable = new TableView<>();

    private ObservableList<Order> ordersData =
            FXCollections.observableArrayList();

    private ObservableList<Product> productsData =
            FXCollections.observableArrayList();

    private Label totalLabel = new Label("Общая стоимость: 0");

    @Override
    public void start(Stage stage) {

        store.setCatalog(
            loadCatalog("catalog.txt")
        );
        createOrdersTable();
        createProductsTable();
        loadOrdersToTable();

        ordersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldOrder, newOrder) -> {

                    productsData.clear();

                    if (newOrder != null) {

                        ProductNode cur =
                                newOrder.getProducts().getHead();

                        while (cur != null) {

                            productsData.add(cur.getData());

                            cur = cur.getNext();
                        }
                    }
                });

        Button addOrderBtn = new Button("Добавить заказ");
        Button removeOrderBtn = new Button("Удалить заказ");

        Button addProductBtn = new Button("Добавить товар");
        Button removeProductBtn = new Button("Удалить товар");

        Button saveBtn = new Button("Сохранить");
        Button loadBtn = new Button("Открыть файл с заказами");

        Button showCatalogBtn = new Button("Показать каталог");

        showCatalogBtn.setOnAction(e -> showCatalog());

        addOrderBtn.setOnAction(e -> addOrder());
        removeOrderBtn.setOnAction(e -> removeOrder());

        addProductBtn.setOnAction(e -> addProduct());
        removeProductBtn.setOnAction(e -> removeProduct());

        saveBtn.setOnAction(e -> save(stage));
        loadBtn.setOnAction(e -> load(stage));

        HBox buttons = new HBox(
                10,
                addOrderBtn,
                removeOrderBtn,
                addProductBtn,
                removeProductBtn,
                saveBtn,
                loadBtn,
                showCatalogBtn
        );

        VBox root = new VBox(
                10,
                new Label("Заказы"),
                ordersTable,
                new Label("Товары заказа"),
                productsTable,
                totalLabel,
                buttons
        );

        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("Интернет-магазин");
        stage.setScene(scene);
        stage.show();

        refreshTotal();
    }

    private void createOrdersTable() {

        TableColumn<Order, String> surnameCol =
                new TableColumn<>("Фамилия");

        surnameCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getCustomerSurname()
                )
        );

        TableColumn<Order, String> dateCol =
                new TableColumn<>("Дата");

        dateCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDate().toString()
                )
        );

        TableColumn<Order, Integer> countCol =
                new TableColumn<>("Кол-во");

        countCol.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getProductsCount()
                )
        );

        TableColumn<Order, Double> costCol =
                new TableColumn<>("Сумма");

        costCol.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().calculateCost()
                )
        );

        ordersTable.getColumns().addAll(
                surnameCol,
                dateCol,
                countCol,
                costCol
        );

        ordersTable.setItems(ordersData);

        ordersTable.setPlaceholder(new Label("Таблица пуста"));
    }

    private void createProductsTable() {

        TableColumn<Product, String> nameCol =
                new TableColumn<>("Название");

        nameCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getName()
                )
        );

        TableColumn<Product, Double> priceCol =
                new TableColumn<>("Цена");

        priceCol.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getPrice()
                )
        );

        productsTable.getColumns().addAll(
                nameCol,
                priceCol
        );

        productsTable.setItems(productsData);

        productsTable.setPlaceholder(new Label("Таблица пуста"));
    }


    private void loadOrdersToTable() {

        ordersData.clear();

        for (int i = 0; i < store.getOrdersList().getSize(); i++) {

            ordersData.add(
                    store.getOrdersList().getOrderByIndex(i)
            );
        }

        refreshTotal();
    }

    private void addOrder() {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Добавление заказа");

        TextField surnameField = new TextField();
        surnameField.setPromptText("Фамилия");

        // ФИЛЬТР ТОЛЬКО БУКВЫ
        surnameField.textProperty().addListener((obs, oldV, newV) -> {

            if (!newV.matches("[а-яА-Яa-zA-Z]*")) {
                surnameField.setText(oldV);
            }
        });

        TextField productField = new TextField();
        productField.setPromptText("Товар");

        VBox box = new VBox(10, surnameField, productField);

        dialog.getDialogPane().setContent(box);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        dialog.showAndWait().ifPresent(btn -> {

            if (btn == ButtonType.OK) {

                try {

                    store.createOrder(
                            surnameField.getText(),
                            productField.getText()
                    );

                    loadOrdersToTable();

                } catch (RuntimeException ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private void removeOrder() {

        Order selected =
                ordersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            error("Выберите заказ");
            return;
        }

        store.removeOrder(selected.getCustomerSurname());

        productsData.clear();

        loadOrdersToTable();
    }

    private void addProduct() {

        Order selected =
                ordersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            error("Выберите заказ");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Добавить товар");

        dialog.setHeaderText("Введите название товара");

        dialog.showAndWait().ifPresent(name -> {

            try {

                store.addProductToOrder(
                        selected.getCustomerSurname(),
                        name
                );

                refreshProducts(selected);

                ordersTable.refresh();

                refreshTotal();

            } catch (RuntimeException ex) {
                error(ex.getMessage());
            }
        });
    }

    private void removeProduct() {

        Order selectedOrder =
                ordersTable.getSelectionModel().getSelectedItem();

        Product selectedProduct =
                productsTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null ||
                selectedProduct == null) {

            error("Выберите товар");
            return;
        }

        store.removeProductFromOrder(
                selectedOrder.getCustomerSurname(),
                selectedProduct.getName()
        );

        Order order =
                store.getOrdersList()
                        .findOrder(selectedOrder.getCustomerSurname());

        if (order == null) {

            productsData.clear();

        } else {

            refreshProducts(order);
        }

        ordersTable.refresh();

        refreshTotal();
    }

    private void refreshProducts(Order order) {

        productsData.clear();

        ProductNode cur =
                order.getProducts().getHead();

        while (cur != null) {

            productsData.add(cur.getData());

            cur = cur.getNext();
        }
    }

    private void refreshTotal() {

        totalLabel.setText(
                "Общая стоимость: " +
                        store.getOrdersList().calculateCost()
        );
    }

    private void error(String msg) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);

        alert.setContentText(msg);

        alert.showAndWait();
    }

    private void save(Stage stage) {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Сохранить");

        File file = chooser.showSaveDialog(stage);

        if (file == null)
            return;

        try (PrintWriter w =
                     new PrintWriter(new FileWriter(file))) {

            for (int i = 0;
                 i < store.getOrdersList().getSize();
                 i++) {

                Order o =
                        store.getOrdersList()
                                .getOrderByIndex(i);

                StringBuilder sb = new StringBuilder();

                sb.append(o.getCustomerSurname())
                        .append(";")
                        .append(o.getDate());

                ProductNode cur =
                        o.getProducts().getHead();

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

            error("Ошибка сохранения");
        }
    }

    private void load(Stage stage) {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Открыть");

        File file = chooser.showOpenDialog(stage);

        if (file == null)
            return;

        OrdersList orders = new OrdersList();

        try (BufferedReader r =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = r.readLine()) != null) {

                String[] parts = line.split(";");

                String surname = parts[0];

                LocalDate date =
                        LocalDate.parse(parts[1]);

                String[] first =
                        parts[2].split(",");

                Product firstProduct =
                        new Product(
                                first[0],
                                Double.parseDouble(first[1])
                        );

                Order order =
                        new Order(
                                surname,
                                date,
                                firstProduct
                        );

                for (int i = 3;
                     i < parts.length;
                     i++) {

                    String[] p =
                            parts[i].split(",");

                    order.addProduct(
                            new Product(
                                    p[0],
                                    Double.parseDouble(p[1])
                            )
                    );
                }

                orders.addOrder(order);
            }

            store.setOrdersList(orders);

            loadOrdersToTable();

            if (!ordersData.isEmpty()) {

                ordersTable.getSelectionModel().selectFirst();

                refreshProducts(
                        ordersData.get(0)
                );
            }

            productsData.clear();

        } catch (IOException e) {

            error("Ошибка загрузки");
        }
    }

    private void showCatalog() {

        TableView<Product> table = new TableView<>();
        ObservableList<Product> data = FXCollections.observableArrayList();

        // Заполняем таблицу товарами из каталога
        ProductNode cur = store.getCatalog().getHead();
        while (cur != null) {
            data.add(cur.getData());
            cur = cur.getNext();
        }

        TableColumn<Product, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        cell.getValue().getName()
                )
        );


        TableColumn<Product, Double> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(
                        cell.getValue().getPrice()
                )
        );

        table.getColumns().addAll(nameCol, priceCol);
        table.setItems(data);

        // Создаем окно
        Stage catalogStage = new Stage();
        VBox root = new VBox(10, new Label("Каталог товаров"), table);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 400, 300);
        catalogStage.setTitle("Каталог");
        catalogStage.setScene(scene);
        catalogStage.show();
    }

    public static void main(String[] args) {
        launch(args);
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
}