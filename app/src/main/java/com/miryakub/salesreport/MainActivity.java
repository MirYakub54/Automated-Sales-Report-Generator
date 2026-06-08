package com.miryakub.salesreport;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int PICK_CSV_REQUEST = 1001;
    private static final int BACKGROUND = Color.rgb(248, 250, 252);
    private static final int INK = Color.rgb(15, 23, 42);
    private static final int MUTED = Color.rgb(71, 85, 105);
    private static final int CARD = Color.WHITE;
    private static final int ACCENT = Color.rgb(15, 118, 110);

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private Report report;
    private String sourceLabel = "Bundled sample_sales.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            report = buildReport(loadBundledSales());
            setContentView(createContent());
        } catch (Exception exception) {
            Toast.makeText(this, "Unable to load sales report", Toast.LENGTH_LONG).show();
            TextView error = new TextView(this);
            error.setText(exception.getMessage());
            error.setPadding(dp(24), dp(24), dp(24), dp(24));
            setContentView(error);
        }
    }

    private ScrollView createContent() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BACKGROUND);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(28), dp(20), dp(28));
        scrollView.addView(container);

        TextView title = text("Sales Report Generator", 28, INK, Typeface.BOLD);
        container.addView(title);

        TextView subtitle = text(
                "Current source: " + sourceLabel,
                15,
                MUTED,
                Typeface.NORMAL
        );
        subtitle.setPadding(0, dp(8), 0, dp(18));
        container.addView(subtitle);

        Button uploadButton = new Button(this);
        uploadButton.setText("Upload CSV File");
        uploadButton.setTextColor(Color.WHITE);
        uploadButton.setBackgroundColor(ACCENT);
        uploadButton.setAllCaps(false);
        uploadButton.setOnClickListener(view -> openCsvPicker());
        container.addView(uploadButton, fullWidthButtonParams(0, dp(16)));

        LinearLayout kpiGrid = new LinearLayout(this);
        kpiGrid.setOrientation(LinearLayout.VERTICAL);
        container.addView(kpiGrid);
        kpiGrid.addView(kpi("Total Orders", String.valueOf(report.totalOrders)));
        kpiGrid.addView(kpi("Units Sold", String.valueOf(report.unitsSold)));
        kpiGrid.addView(kpi("Net Revenue", currency.format(report.netRevenue)));
        kpiGrid.addView(kpi("Average Order", currency.format(report.averageOrderValue)));

        container.addView(section("Revenue by Region"));
        addSummaryRows(container, report.revenueByRegion);

        container.addView(section("Revenue by Category"));
        addSummaryRows(container, report.revenueByCategory);

        container.addView(section("Monthly Trend"));
        addSummaryRows(container, report.revenueByMonth);

        Button shareButton = new Button(this);
        shareButton.setText("Share Report");
        shareButton.setTextColor(Color.WHITE);
        shareButton.setBackgroundColor(ACCENT);
        shareButton.setAllCaps(false);
        shareButton.setOnClickListener(view -> shareReport());
        container.addView(shareButton, fullWidthButtonParams(dp(18), 0));

        return scrollView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_CSV_REQUEST || resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri uri = data.getData();
        if (uri == null) {
            return;
        }

        try {
            report = buildReport(loadSalesFromUri(uri));
            sourceLabel = "Uploaded CSV";
            setContentView(createContent());
            Toast.makeText(this, "Report generated from uploaded file", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "CSV error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private TextView section(String label) {
        TextView view = text(label, 20, INK, Typeface.BOLD);
        view.setPadding(0, dp(24), 0, dp(10));
        return view;
    }

    private LinearLayout kpi(String label, String value) {
        LinearLayout card = card();
        TextView metric = text(label, 14, MUTED, Typeface.NORMAL);
        TextView amount = text(value, 24, INK, Typeface.BOLD);
        amount.setPadding(0, dp(4), 0, 0);
        card.addView(metric);
        card.addView(amount);
        return card;
    }

    private void addSummaryRows(LinearLayout container, Map<String, Double> rows) {
        for (Map.Entry<String, Double> entry : rows.entrySet()) {
            LinearLayout row = card();
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView name = text(entry.getKey(), 16, INK, Typeface.BOLD);
            TextView amount = text(currency.format(entry.getValue()), 16, ACCENT, Typeface.BOLD);
            amount.setGravity(Gravity.END);

            row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            row.addView(amount, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            container.addView(row);
        }
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(CARD);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);
        card.setElevation(dp(1));
        return card;
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private LinearLayout.LayoutParams fullWidthButtonParams(int topMargin, int bottomMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, topMargin, 0, bottomMargin);
        return params;
    }

    private void openCsvPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "text/comma-separated-values", "text/plain"});
        startActivityForResult(intent, PICK_CSV_REQUEST);
    }

    private List<Sale> loadBundledSales() throws Exception {
        try (InputStream stream = getAssets().open("sample_sales.csv")) {
            return loadSales(stream);
        }
    }

    private List<Sale> loadSalesFromUri(Uri uri) throws Exception {
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            if (stream == null) {
                throw new IllegalArgumentException("Unable to open selected file");
            }
            return loadSales(stream);
        }
    }

    private List<Sale> loadSales(InputStream stream) throws Exception {
        List<Sale> sales = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV is empty");
            }

            Map<String, Integer> columns = columnIndexes(parseCsvLine(headerLine));
            requireColumns(columns);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> parts = parseCsvLine(line);
                Sale sale = new Sale();
                sale.orderId = value(parts, columns, "order_id");
                sale.orderMonth = monthName(value(parts, columns, "order_date"));
                sale.region = value(parts, columns, "region");
                sale.category = value(parts, columns, "category");
                sale.product = value(parts, columns, "product");
                sale.quantity = Integer.parseInt(value(parts, columns, "quantity"));
                sale.unitPrice = Double.parseDouble(value(parts, columns, "unit_price"));
                sale.discount = Double.parseDouble(value(parts, columns, "discount"));
                sales.add(sale);
            }
        }

        if (sales.isEmpty()) {
            throw new IllegalArgumentException("CSV has no sales rows");
        }
        return sales;
    }

    private List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (character == ',' && !inQuotes) {
                cells.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        cells.add(current.toString().trim());
        return cells;
    }

    private Map<String, Integer> columnIndexes(List<String> headers) {
        Map<String, Integer> indexes = new HashMap<>();
        for (int index = 0; index < headers.size(); index++) {
            indexes.put(headers.get(index).trim().toLowerCase(Locale.US), index);
        }
        return indexes;
    }

    private void requireColumns(Map<String, Integer> columns) {
        String[] required = {"order_id", "order_date", "region", "category", "product", "quantity", "unit_price", "discount"};
        for (String column : required) {
            if (!columns.containsKey(column)) {
                throw new IllegalArgumentException("Missing column: " + column);
            }
        }
    }

    private String value(List<String> parts, Map<String, Integer> columns, String column) {
        int index = columns.get(column);
        return index < parts.size() ? parts.get(index).trim() : "";
    }

    private Report buildReport(List<Sale> sales) {
        Report result = new Report();
        result.totalOrders = sales.size();

        for (Sale sale : sales) {
            double net = sale.quantity * sale.unitPrice * (1 - sale.discount);
            result.unitsSold += sale.quantity;
            result.netRevenue += net;
            add(result.revenueByRegion, sale.region, net);
            add(result.revenueByCategory, sale.category, net);
            add(result.revenueByMonth, sale.orderMonth, net);
        }

        result.averageOrderValue = result.totalOrders == 0 ? 0 : result.netRevenue / result.totalOrders;
        return result;
    }

    private String monthName(String date) {
        String separator = date.contains("-") ? "-" : "/";
        String[] parts = date.split(separator);
        if (parts.length < 2) {
            return "Unknown";
        }

        String[] months = {
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };
        try {
            int month = Integer.parseInt(parts[0].length() == 4 ? parts[1] : parts[1]);
            return month >= 1 && month <= 12 ? months[month - 1] : "Unknown";
        } catch (NumberFormatException exception) {
            return "Unknown";
        }
    }

    private void add(Map<String, Double> map, String key, double value) {
        map.put(key, map.getOrDefault(key, 0.0) + value);
    }

    private void shareReport() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Automated Sales Report");
        intent.putExtra(Intent.EXTRA_TEXT, report.asText(currency));
        startActivity(Intent.createChooser(intent, "Share Sales Report"));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Sale {
        String orderId;
        String orderMonth;
        String region;
        String category;
        String product;
        int quantity;
        double unitPrice;
        double discount;
    }

    private static class Report {
        int totalOrders;
        int unitsSold;
        double netRevenue;
        double averageOrderValue;
        Map<String, Double> revenueByRegion = new LinkedHashMap<>();
        Map<String, Double> revenueByCategory = new LinkedHashMap<>();
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();

        String asText(NumberFormat currency) {
            StringBuilder builder = new StringBuilder();
            builder.append("Automated Sales Report\n\n");
            builder.append("Total Orders: ").append(totalOrders).append('\n');
            builder.append("Units Sold: ").append(unitsSold).append('\n');
            builder.append("Net Revenue: ").append(currency.format(netRevenue)).append('\n');
            builder.append("Average Order: ").append(currency.format(averageOrderValue)).append("\n\n");
            appendSection(builder, "Revenue by Region", revenueByRegion, currency);
            appendSection(builder, "Revenue by Category", revenueByCategory, currency);
            appendSection(builder, "Monthly Trend", revenueByMonth, currency);
            return builder.toString();
        }

        private static void appendSection(
                StringBuilder builder,
                String title,
                Map<String, Double> rows,
                NumberFormat currency
        ) {
            builder.append(title).append('\n');
            for (Map.Entry<String, Double> entry : rows.entrySet()) {
                builder.append("- ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(currency.format(entry.getValue()))
                        .append('\n');
            }
            builder.append('\n');
        }
    }
}
