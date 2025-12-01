FROM eclipse-temurin:17-jre-jammy

# Install dependencies for adding PPA
RUN apt-get update && apt-get install -y \
    software-properties-common \
    gpg-agent \
    && rm -rf /var/lib/apt/lists/*

# Add Mozilla PPA and configure apt to prefer it over snap
RUN add-apt-repository -y ppa:mozillateam/ppa \
    && echo 'Package: *\nPin: release o=LP-PPA-mozillateam\nPin-Priority: 1001' > /etc/apt/preferences.d/mozilla-firefox \
    && echo 'Unattended-Upgrade::Allowed-Origins:: "LP-PPA-mozillateam:${distro_codename}";' > /etc/apt/apt.conf.d/51unattended-upgrades-firefox

# Install Firefox and geckodriver dependencies
RUN apt-get update && apt-get install -y \
    firefox \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Download and install geckodriver
RUN wget -q https://github.com/mozilla/geckodriver/releases/download/v0.35.0/geckodriver-v0.35.0-linux64.tar.gz \
    && tar -xzf geckodriver-v0.35.0-linux64.tar.gz -C /usr/local/bin \
    && chmod +x /usr/local/bin/geckodriver \
    && rm geckodriver-v0.35.0-linux64.tar.gz

# Verify installation
RUN firefox --version && geckodriver --version

WORKDIR /app
COPY target/job-monitor-1.0.0.jar app.jar

ENV MOZ_HEADLESS=1

ENTRYPOINT ["java", "-jar", "app.jar"]