# Hedera Asset Tracker

A real-time dashboard for tracking token and NFT transactions on the Hedera network, with alerts for high-value transfers and whale activity.

## Features

- **Real-time Transaction Tracking**: Monitor live transactions of tokens and NFTs on the Hedera network
- **Token Analytics**: View transaction volumes, market caps, and price changes
- **Alert System**: Get notified of high-value transfers and whale activity
- **Dashboard**: Interactive UI with graphs and real-time updates
- **WebSocket Support**: Instant updates without refreshing the page

## Technology Stack

- Java 11
- Spring Boot 2.7.8
- Spring Data JPA
- Spring WebSocket
- H2 Database
- Thymeleaf
- Bootstrap 5
- Hedera Java SDK
- Hedera Mirror Node API

## Setup Instructions

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/your-javaguy/hedera-tracker.git
   cd hedera-tracker
   ```

2. Build the application:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn spring-boot:run
   ```

4. Access the dashboard:
   ```
   http://localhost:8080
   ```

### Configuration

You can customize the application by modifying the `application.properties` file. Key settings include:

- `hedera.mirror.api.baseUrl`: The URL of the Hedera Mirror Node API
- `hedera.refresh.interval.seconds`: How frequently to fetch new transactions

## Usage

### Dashboard

The main dashboard provides an overview of:
- Recent transactions
- Top tokens by activity
- Active alerts
- Key statistics

### Tokens Page

View all tracked tokens with detailed information:
- Transaction count
- Market cap
- Type (fungible or NFT)

### Transactions Page

Browse and filter transactions:
- Filter by token
- View high-value transfers
- Examine whale activity

### Alerts Page

Manage and acknowledge alerts:
- High-value transfer alerts
- Whale activity alerts
- Filter by time period

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Hedera Hashgraph](https://hedera.com)
- [Hedera Mirror Node](https://docs.hedera.com/guides/mirrornet/hedera-mirror-node) 