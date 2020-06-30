using RabbitMQ.Client;
using System;
using System.Text;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Services
{
    public class MsgQueueService 
    {
        private ConnectionFactory factory;
        private IConnection connection;
        private IModel channel;
        public MsgQueueService()
        {
            factory = new ConnectionFactory() { 
                Uri = new Uri("amqp://vcqubngz:jyA59K9eMnlB7zuqfh73lr5WeEPLjQ89@stingray.rmq.cloudamqp.com/vcqubngz") 
            };
        }
        private void Connect() {
            connection = factory.CreateConnection();
            channel = connection.CreateModel();

            channel.ExchangeDeclare(exchange: "arso-exchange", 
                                type: "direct", 
                                durable: true, 
                                autoDelete: false, 
                                arguments: null);

            channel.QueueDeclare(queue: "arso-queue",
                                durable: true,
                                exclusive: false,
                                autoDelete: false,
                                arguments: null);

            channel.QueueBind(queue: "arso-queue", 
                                exchange: "arso-exchange", 
                                routingKey: "arso-queue");
        }

        private void Disconnect() {
            channel.Close();
            connection.Close();
        }

        public void ProduceMessage(string message) {
            Connect();

            var body = Encoding.UTF8.GetBytes(message);

            IBasicProperties props = channel.CreateBasicProperties();
            props.ContentType = "text/plain";
            
            channel.BasicPublish(exchange: "arso-exchange",
                                routingKey: "arso-queue",
                                basicProperties: props,
                                body: body);

            Disconnect();
        }
    }
}
