package com.store;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class Main extends JFrame {
    private ProductDAO productDAO;
    private JTable productsTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private JComboBox<String> categoryFilter;

    public Main() {
        initializeDAO();
        setupUI();
        loadProducts();
    }

    private void initializeDAO() {
        try {
            productDAO = new ProductDAO();
            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo conectar a la base de datos.\nVerifica que MySQL esté ejecutándose.", 
                    "Error de Conexión", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al inicializar: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupUI() {
        setTitle("🛍️ Tienda Segunda Mano - Sistema de Gestión de Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Panel principal con diseño moderno
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Centro con tabla y controles
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        
        // Panel de estadísticas
        mainPanel.add(createStatsPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(59, 89, 152));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Título
        JLabel titleLabel = new JLabel("GESTIÓN DE INVENTARIO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        // Botones con diseño flat moderno
        addStyledButton(buttonPanel, "NUEVO PRODUCTO", new Color(46, 204, 113), e -> showAddProductDialog());
        addStyledButton(buttonPanel, "EDITAR", new Color(52, 152, 219), e -> editSelectedProduct());
        addStyledButton(buttonPanel, "ELIMINAR", new Color(231, 76, 60), e -> deleteSelectedProduct());
        addStyledButton(buttonPanel, "ACTUALIZAR", new Color(155, 89, 182), e -> refreshData());

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void addStyledButton(JPanel panel, String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover suave
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.brighter(), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker(), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
        });
        
        button.addActionListener(listener);
        panel.add(button);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Panel de filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Filtrar por categoría:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String[] categories = {"Todas", "Muebles", "Música", "Electrónica", "Ropa", "Decoración", 
                              "Libros", "Hogar", "Deportes", "Juguetes", "Joyería"};
        categoryFilter = new JComboBox<>(categories);
        categoryFilter.addActionListener(e -> filterByCategory());

        filterPanel.add(filterLabel);
        filterPanel.add(categoryFilter);

        // Barra de búsqueda
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar productos...");
        JButton searchButton = new JButton("🔍 Buscar");
        searchButton.addActionListener(e -> searchProducts(searchField.getText()));

        filterPanel.add(new JLabel("Buscar:"));
        filterPanel.add(searchField);
        filterPanel.add(searchButton);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Tabla de productos
        String[] columnNames = {"ID", "Nombre", "Precio (€)", "Stock", "Categoría", "Descripción"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable directamente
            }
        };

        productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.setRowHeight(30);
        productsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        productsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        productsTable.setShowGrid(true);
        productsTable.setGridColor(new Color(240, 240, 240));

        // Renderer para números y stock bajo
        productsTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(productsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsPanel.setBackground(new Color(240, 240, 240));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        statsLabel = new JLabel("Cargando estadísticas...");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(52, 73, 94));

        statsPanel.add(statsLabel);

        return statsPanel;
    }

    private void addButton(JPanel panel, String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.addActionListener(listener);
        
        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        panel.add(button);
    }

    private void loadProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            updateTable(products);
            updateStatistics(products);
        } catch (SQLException e) {
            showError("Error al cargar productos: " + e.getMessage());
        }
    }

    private void updateTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            Object[] row = {
                product.getId(),
                product.getName(),
                String.format("%.2f", product.getPrice()),
                product.getStock(),
                product.getCategory(),
                product.getDescription()
            };
            tableModel.addRow(row);
        }
    }

    private void updateStatistics(List<Product> products) {
        int totalProducts = products.size();
        long lowStock = products.stream().filter(p -> p.getStock() < 3).count();
        double totalValue = products.stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();

        String stats = String.format(
            "Estadísticas: %d productos | %d con stock bajo | Valor total: %.2f€",
            totalProducts, lowStock, totalValue
        );
        statsLabel.setText(stats);
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Agregar Nuevo Producto", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
            "Muebles", "Música", "Electrónica", "Ropa", "Decoración", 
            "Libros", "Hogar", "Deportes", "Juguetes", "Joyería"
        });
        JTextArea descriptionArea = new JTextArea(3, 20);

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Precio (€):"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Categoría:"));
        formPanel.add(categoryCombo);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(new JScrollPane(descriptionArea));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("💾 Guardar");
        JButton cancelButton = new JButton("❌ Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Product newProduct = new Product(
                    nameField.getText(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(stockField.getText()),
                    (String) categoryCombo.getSelectedItem(),
                    descriptionArea.getText()
                );

                productDAO.addProduct(newProduct);
                loadProducts();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Producto agregado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (InvalidProductException ex) {
                showError("Error de validación: " + ex.getMessage());
            } catch (SQLException ex) {
                showError("Error de base de datos: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                showError("Por favor ingrese valores numéricos válidos para precio y stock");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Por favor seleccione un producto para editar");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Product product = productDAO.getProductById(productId);
            if (product != null) {
                showEditProductDialog(product);
            }
        } catch (SQLException e) {
            showError("Error al cargar el producto: " + e.getMessage());
        }
    }

    private void showEditProductDialog(Product product) {
        JDialog dialog = new JDialog(this, "Editar Producto", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(product.getName());
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        JTextField stockField = new JTextField(String.valueOf(product.getStock()));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
            "Muebles", "Música", "Electrónica", "Ropa", "Decoración", 
            "Libros", "Hogar", "Deportes", "Juguetes", "Joyería"
        });
        categoryCombo.setSelectedItem(product.getCategory());
        JTextArea descriptionArea = new JTextArea(product.getDescription(), 3, 20);

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Precio (€):"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Categoría:"));
        formPanel.add(categoryCombo);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(new JScrollPane(descriptionArea));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("💾 Guardar Cambios");
        JButton cancelButton = new JButton("❌ Cancelar");

        saveButton.addActionListener(e -> {
            try {
                product.setName(nameField.getText());
                product.setPrice(Double.parseDouble(priceField.getText()));
                product.setStock(Integer.parseInt(stockField.getText()));
                product.setCategory((String) categoryCombo.getSelectedItem());
                product.setDescription(descriptionArea.getText());

                productDAO.updateProduct(product);
                loadProducts();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Producto actualizado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (InvalidProductException ex) {
                showError("Error de validación: " + ex.getMessage());
            } catch (SQLException ex) {
                showError("Error de base de datos: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                showError("Por favor ingrese valores numéricos válidos para precio y stock");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Por favor seleccione un producto para eliminar");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar el producto:\n\"" + productName + "\"?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productDAO.deleteProduct(productId);
                loadProducts();
                JOptionPane.showMessageDialog(this, "Producto eliminado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                showError("Error al eliminar el producto: " + e.getMessage());
            }
        }
    }

    private void filterByCategory() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        if ("Todas".equals(selectedCategory)) {
            loadProducts();
        } else {
            try {
                List<Product> allProducts = productDAO.getAllProducts();
                List<Product> filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategory().equals(selectedCategory))
                    .toList();
                updateTable(filteredProducts);
                updateStatistics(filteredProducts);
            } catch (SQLException e) {
                showError("Error al filtrar productos: " + e.getMessage());
            }
        }
    }

    private void searchProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadProducts();
            return;
        }

        try {
            List<Product> allProducts = productDAO.getAllProducts();
            List<Product> filteredProducts = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                           p.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                .toList();
            updateTable(filteredProducts);
            updateStatistics(filteredProducts);
        } catch (SQLException e) {
            showError("Error al buscar productos: " + e.getMessage());
        }
    }

    private void refreshData() {
        loadProducts();
        JOptionPane.showMessageDialog(this, "Datos actualizados", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Renderer personalizado para la tabla
 // Renderer personalizado para la tabla - VERSIÓN COMPLETA
    private static class CustomTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Resaltar stock bajo
            if (column == 3 && value instanceof Integer) {
                int stock = (Integer) value;
                if (stock < 3) {
                    c.setBackground(new Color(255, 230, 230)); // Rojo claro
                    c.setForeground(Color.RED);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (stock < 10) {
                    c.setBackground(new Color(255, 255, 200)); // Amarillo claro
                    c.setForeground(new Color(153, 102, 0));
                } else {
                    c.setBackground(new Color(230, 255, 230)); // Verde claro
                    c.setForeground(Color.BLACK);
                }
            } else {
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                c.setForeground(Color.BLACK);
            }

            // Alinear contenido
            if (column == 0 || column == 2 || column == 3) { // ID, Precio, Stock
                setHorizontalAlignment(JLabel.RIGHT);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }

            return c;
        }
    }

    public static void main(String[] args) {
        // Establecer look and feel moderno
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Personalizar colores
            UIManager.put("Table.selectionBackground", new Color(52, 152, 219));
            UIManager.put("Table.selectionForeground", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}