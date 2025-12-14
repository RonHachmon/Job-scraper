# Job Scraper

A Java-based job scraper application designed to automatically monitor and collect job postings from various sources. Built with Maven and Docker support for easy deployment.

## Features

- 🔍 **Automated Job Monitoring** - Continuously scrapes job listings from configured sources
- 🐳 **Docker Support** - Fully containerized application with Docker and Docker Compose
- ⚙️ **Configurable** - Easy configuration through environment variables
- 🔄 **Concurrent Processing** - Efficient multi-threaded scraping
- 📊 **Data Collection** - Structured job data extraction and storage

## Technologies

- **Java** - Core application language
- **Maven** - Build automation and dependency management
- **Docker** - Containerization for consistent deployment
- **Docker Compose** - Multi-container orchestration

## Prerequisites

Before running this project, ensure you have the following installed:

- Java 11 or higher
- Maven 3.6+
- Docker (for containerized deployment)
- Docker Compose (optional, for orchestrated deployment)

## Installation

### Local Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/RonHachmon/Job-scraper.git
   cd Job-scraper
   ```

2. **Configure environment variables**
   ```bash
   cp .template.env .env
   # Edit .env with your configuration
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn exec:java
   ```

### Docker Setup

1. **Clone and configure**
   ```bash
   git clone https://github.com/RonHachmon/Job-scraper.git
   cd Job-scraper
   cp .template.env .env
   # Edit .env with your configuration
   ```

2. **Build the Docker image**
   ```bash
   docker build -t job-scraper .
   ```

3. **Run with Docker**
   ```bash
   docker run --env-file .env job-scraper
   ```

### Docker Compose Setup

For the easiest deployment:

```bash
docker-compose up -d
```

To stop the application:

```bash
docker-compose down
```

## Configuration

Configuration is managed through environment variables. Copy the `.template.env` file to `.env` and customize the following variables:

```env
# Add your configuration variables here
# Examples:
# SCRAPE_INTERVAL=3600
# MAX_THREADS=5
# DATABASE_URL=jdbc:postgresql://localhost:5432/jobs
```

See `.template.env` for all available configuration options.

## Project Structure

```
Job-scraper/
├── src/
│   └── main/
│       ├── java/           # Java source files
│       └── resources/      # Application resources
├── .gitignore
├── .template.env          # Environment variable template
├── Dockerfile             # Docker container definition
├── docker-compose.yaml    # Docker Compose configuration
├── pom.xml               # Maven project configuration
├── LICENSE               # MIT License
└── README.md            # This file
```

## Usage

### Running the Scraper

Once configured and running, the application will:

1. Connect to configured job sources
2. Extract job posting information
3. Process and store the data
4. Continue monitoring based on configured intervals

### Monitoring Logs

**Docker:**
```bash
docker logs -f <container-name>
```

**Docker Compose:**
```bash
docker-compose logs -f
```

## Development

### Building from Source

```bash
# Clean build
mvn clean compile

# Run tests
mvn test

# Package application
mvn package
```

### Code Structure

The application follows a modular architecture:

- **Scrapers** - Job source specific scraping logic
- **Parsers** - Data extraction and transformation
- **Storage** - Data persistence layer
- **Schedulers** - Job execution timing

## Docker Details

### Dockerfile

The Dockerfile creates a lightweight container with:
- Java runtime environment
- Application JAR
- Required dependencies

### docker-compose.yaml

The Docker Compose configuration orchestrates:
- Application container
- Environment variable injection
- Volume mounting (if needed)
- Network configuration


## Troubleshooting

### Common Issues

**Build fails:**
```bash
# Clean Maven cache and rebuild
mvn clean install -U
```

**Docker container won't start:**
```bash
# Check logs for errors
docker logs <container-name>

# Verify environment variables
docker inspect <container-name>
```

**Connection issues:**
- Verify network configuration in docker-compose.yaml
- Check firewall settings
- Ensure API keys/credentials are correct in .env

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with modern web scraping best practices
- Designed for efficient job market monitoring
- Containerized for easy deployment and scaling

## Contact

Ron Hachmon - [@RonHachmon](https://github.com/RonHachmon)

Project Link: [https://github.com/RonHachmon/Job-scraper](https://github.com/RonHachmon/Job-scraper)

---

**Note:** This tool is intended for personal use and educational purposes. Please respect the terms of service of any websites you scrape and implement appropriate rate limiting.