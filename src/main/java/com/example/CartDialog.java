package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CartDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private Cart cart;
    private JComboBox<String> orderTypeComboBox;
    private JComboBox<String> paymentMethodComboBox;

    public CartDialog(JFrame parent, Cart cart) {
        super(parent, "Keranjang", true);
        this.cart = cart;

        setSize(500, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Menu", "Qty", "Harga", "Total"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel bawah untuk pilihan dan tombol
        JPanel bottomPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        // ComboBox untuk Jenis Pesanan
        bottomPanel.add(new JLabel("Jenis Pesanan:"));
        orderTypeComboBox = new JComboBox<>(new String[]{"Dine In", "Takeaway"});
        bottomPanel.add(orderTypeComboBox);

        // ComboBox untuk Metode Pembayaran
        bottomPanel.add(new JLabel("Metode Pembayaran:"));
        paymentMethodComboBox = new JComboBox<>(new String[]{"Cash", "Credit Card", "E-Wallet"});
        bottomPanel.add(paymentMethodComboBox);

        // Tombol Checkout
        JButton checkoutBtn = new JButton("Checkout");
        bottomPanel.add(checkoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        loadData();

        checkoutBtn.addActionListener(e -> {
            checkout();
            dispose();
        });
    }

    private void loadData() {
        model.setRowCount(0);
        for (CartItem item : cart.getItems()) {
            Vector<Object> row = new Vector<>();
            row.add(item.getName());
            row.add(item.getQuantity());
            row.add(item.getPrice());
            row.add(item.getTotalPrice());
            model.addRow(row);
        }
    }

    private void checkout() {
        try (Connection conn = KoneksiDB.getConnection()) {
            conn.setAutoCommit(false);

            String orderType = orderTypeComboBox.getSelectedItem().toString();
            String paymentMethod = paymentMethodComboBox.getSelectedItem().toString();

            // Simpan order ke tabel orders
            String insertOrder = "INSERT INTO orders (total_price, order_type) VALUES (?, ?)";
            PreparedStatement psOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setBigDecimal(1, cart.getTotal());
            psOrder.setString(2, orderType.toLowerCase());
            psOrder.executeUpdate();

            var rs = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // Simpan ke tabel order_items
            String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            Statement st = conn.createStatement();

            for (CartItem item : cart.getItems()) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getMenuId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getPrice());
                ps.addBatch();

                // Kurangi stok menu
                st.executeUpdate("UPDATE menu_items SET stock = stock - " + item.getQuantity() +
                        " WHERE id = " + item.getMenuId());
            }

            ps.executeBatch();

            // Simpan metode pembayaran ke tabel payments
            String paymentSQL = "INSERT INTO payments (order_id, amount, payment_method) VALUES (?, ?, ?)";
            PreparedStatement psPay = conn.prepareStatement(paymentSQL);
            psPay.setInt(1, orderId);
            psPay.setBigDecimal(2, cart.getTotal());
            psPay.setString(3, paymentMethod.toLowerCase().replace(" ", "_"));
            psPay.executeUpdate();

            conn.commit();

            JOptionPane.showMessageDialog(this, "Checkout berhasil!");
            cart.clear();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Checkout gagal: " + e.getMessage());
        }
    }
}
