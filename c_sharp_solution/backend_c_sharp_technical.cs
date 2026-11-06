using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Net;
using System.Net.Mail;

namespace RefactoringExercise
{
    public class Order
    {
        public int Id { get; set; }
        public int CustomerId { get; set; }
        public decimal Total { get; set; }
        public string Status { get; set; } = string.Empty;
        public string PaymentMethod { get; set; } = string.Empty;
    }

    public class Product
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public decimal Price { get; set; }
    }

    public class ProcessRequest
    {
        public int CustomerId { get; set; }
        public string CustomerEmail { get; set; } = string.Empty;
        public List<string> Items { get; set; } = new();
        public string PaymentMethod { get; set; } = string.Empty;
        public decimal Discount { get; set; }
    }

    internal class OrderRepository
    {
        private const string ConnectionString =
            "Server=localhost;Database=Orders;User Id=sa;******;";

        public double GetProductPrice(string item)
        {
            using (var connection = new SqlConnection(ConnectionString))
            {
                connection.Open();
                var command = new SqlCommand(
                    "SELECT Price FROM Products WHERE Name = '" + item + "'", connection);
                return (double)command.ExecuteScalar();
            }
        }

        public void SaveOrder(int customerId, double total, string paymentMethod)
        {
            using (var connection = new SqlConnection(ConnectionString))
            {
                connection.Open();
                var command = new SqlCommand(
                    "INSERT INTO Orders (CustomerId, Total, Status, PaymentMethod) VALUES (" +
                    customerId + ", " + total + ", 'Completed', '" + paymentMethod + "')",
                    connection);
                command.ExecuteNonQuery();
            }
        }

        public List<string> FindHistory(string customerId)
        {
            var orders = new List<string>();
            using (var connection = new SqlConnection(ConnectionString))
            {
                connection.Open();
                var command = new SqlCommand(
                    "SELECT * FROM Orders WHERE CustomerId = " + customerId, connection);
                using (var reader = command.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        orders.Add("Order #" + reader["Id"] + " - $" + reader["Total"] +
                                   " - " + reader["Status"]);
                    }
                }
            }

            return orders;
        }
    }

    internal class PaymentProcessor
    {
        public string ProcessPayment(string method, double total)
        {
            if (method == "CreditCard")
            {
                var client = new WebClient();
                return client.DownloadString(
                    "https://api.payment.com/charge?amount=" + total);
            }

            if (method == "PayPal")
            {
                var client = new WebClient();
                return client.DownloadString(
                    "https://api.paypal.com/charge?amount=" + total);
            }

            if (method == "BankTransfer")
            {
                return "pending";
            }

            return "Invalid payment method";
        }
    }

    internal class OrderConfirmationEmailSender
    {
        public void Send(string customerEmail, double total)
        {
            var smtp = new SmtpClient("smtp.gmail.com", 587);
            smtp.Credentials = new NetworkCredential("noreply@company.com", "P@ssw0rd123");
            smtp.EnableSsl = true;

            var mail = new MailMessage();
            mail.From = new MailAddress("noreply@company.com");
            mail.To.Add(customerEmail);
            mail.Subject = "Order Confirmation";
            mail.Body = "Your order has been placed. Total: $" + total;

            smtp.Send(mail);
        }
    }

    public class OrderProcessor
    {
        public string DoProcess(ProcessRequest request)
        {
            var repository = new OrderRepository();
            double total = 0;
            foreach (var item in request.Items)
            {
                total += repository.GetProductPrice(item);
            }

            if (request.Discount > 0)
            {
                total -= total * (double)request.Discount;
            }

            var paymentResult = new PaymentProcessor().ProcessPayment(request.PaymentMethod, total);
            if (paymentResult == "Invalid payment method")
            {
                return paymentResult;
            }

            if (paymentResult.Contains("success") || paymentResult == "pending")
            {
                repository.SaveOrder(request.CustomerId, total, request.PaymentMethod);

                try
                {
                    new OrderConfirmationEmailSender().Send(request.CustomerEmail, total);
                    return "Order processed successfully. Total: $" + total;
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Email failed: " + ex.Message);
                    return "Order processed but email failed. Total: $" + total;
                }
            }

            return "Payment failed";
        }

        public List<string> FindHistory(string customer_id)
        {
            return new OrderRepository().FindHistory(customer_id);
        }
    }
}
