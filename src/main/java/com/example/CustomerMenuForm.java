package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class CustomerMenuForm extends JFrame {
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private Cart cart = new Cart();

    public CustomerMenuForm() {
        setTitle("Pilih Menu");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Harga", "Stok"}, 0);
        menuTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(menuTable);

        JButton tambahBtn = new JButton("Tambah ke Keranjang");
        JButton keranjangBtn = new JButton("Lihat Keranjang");

        tambahBtn.addActionListener(e -> tambahKeKeranjang());
        keranjangBtn.addActionListener(e -> new CartDialog(this, cart).setVisible(true));

        JPanel btnPanel = new JPanel();
        btnPanel.add(tambahBtn);
        btnPanel.add(keranjangBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loadMenu();
        setVisible(true);
    }

    private void loadMenu() {
        tableModel.setRowCount(0);
        try (Connection conn = KoneksiDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM menu_items")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void tambahKeKeranjang() {
        int selected = menuTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Pilih menu terlebih dahulu!");
            return;
        }

        int menuId = (int) tableModel.getValueAt(selected, 0);
        String name = (String) tableModel.getValueAt(selected, 1);
        BigDecimal price = (BigDecimal) tableModel.getValueAt(selected, 2);

        String jumlahStr = JOptionPane.showInputDialog(this, "Masukkan jumlah:");
        if (jumlahStr != null && !jumlahStr.isEmpty()) {
            try {
                int jumlah = Integer.parseInt(jumlahStr);
                if (jumlah <= 0) throw new NumberFormatException();

                cart.addItem(new CartItem(menuId, name, price, jumlah));
                JOptionPane.showMessageDialog(this, "Item ditambahkan ke keranjang!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Jumlah tidak valid!");
            }
        }
    }
}
