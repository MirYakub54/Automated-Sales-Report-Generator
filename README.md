# Automated Sales Report Generator

Python automation project that reads raw CSV sales data, cleans it with Pandas, generates multi-dimensional revenue analysis, exports a polished multi-sheet Excel report with OpenPyXL charts and KPI summaries, and can email the finished report through Gmail SMTP using secure TLS.

## Features

- Cleans raw CSV sales data by parsing dates, normalizing text fields, removing duplicate orders, and validating numeric columns.
- Performs revenue analysis by region, category, product, and month.
- Generates a professional Excel workbook with:
  - cleaned transaction data
  - KPI summary sheet
  - regional revenue analysis
  - category revenue analysis
  - top product ranking
  - monthly trend analysis
  - bar and line charts
- Supports optional Gmail SMTP delivery with credentials supplied through environment variables.

## Quick Start

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python sales_reporter.py
```

The generated report is saved to:

```text
output/sales_report.xlsx
```

## Use Your Own CSV

Your CSV should include these columns:

```text
order_id, order_date, customer, region, category, product, quantity, unit_price, discount
```

Run:

```powershell
python sales_reporter.py --input path\to\sales.csv --output output\monthly_sales_report.xlsx
```

## Email Delivery

For Gmail, create an app password, then set these variables:

```powershell
$env:SMTP_SENDER="your.email@gmail.com"
$env:SMTP_PASSWORD="your-gmail-app-password"
$env:SMTP_RECIPIENTS="manager@example.com,analyst@example.com"
python sales_reporter.py --email
```

Optional variables:

```powershell
$env:SMTP_HOST="smtp.gmail.com"
$env:SMTP_PORT="587"
```

You can also copy `.env.example` to `.env` for your own reference. Do not commit real passwords or app passwords.

## Scheduled Delivery

The pipeline can be scheduled with Windows Task Scheduler by creating a task that runs this command from the project folder:

```powershell
python sales_reporter.py --input sample_sales.csv --output output\sales_report.xlsx --email
```

Use a daily, weekly, or monthly trigger depending on how often new sales CSV files are available.

## Project Structure

```text
.
|-- sales_reporter.py     # Main automation pipeline
|-- sample_sales.csv      # Demo input data
|-- requirements.txt      # Python dependencies
|-- .env.example          # SMTP configuration template
|-- .gitignore            # Keeps generated reports and secrets out of Git
`-- README.md             # Setup and usage guide
```

## Resume Bullet

Built an end-to-end sales reporting automation pipeline using Python, Pandas, OpenPyXL, and smtplib. The tool reads raw CSV data, cleans and validates records, performs multi-dimensional revenue analysis, generates chart-rich Excel reports with KPI summaries, and optionally delivers reports through Gmail SMTP with TLS in a single command.
