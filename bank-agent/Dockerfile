FROM eclipse-temurin:17-jdk-jammy

# Установка зависимостей и Chrome
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    ca-certificates \
    unzip \
    xvfb \
    libnss3-tools \
    libx11-xcb1 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxi6 \
    libxtst6 \
    libxrandr2 \
    libglib2.0-0 \
    libgtk-3-0 \
    libxss1 \
    libasound2 \
    fonts-liberation \
    libappindicator3-1 \
    libnss3 \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome-keyring.gpg \
    && echo "deb [signed-by=/usr/share/keyrings/google-chrome-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Установка совместимого ChromeDriver
RUN CHROME_VERSION=$(google-chrome --version | grep -oP '\d+\.\d+\.\d+\.\d+') && \
    CHROME_MAJOR=$(echo $CHROME_VERSION | cut -d. -f1) && \
    echo "Chrome version: $CHROME_VERSION (major: $CHROME_MAJOR)" && \
    DRIVER_VERSION=$(wget -qO- https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${CHROME_MAJOR}) && \
    echo "ChromeDriver version: $DRIVER_VERSION" && \
    wget -q https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/${DRIVER_VERSION}/linux64/chromedriver-linux64.zip -O /tmp/chromedriver.zip && \
    unzip /tmp/chromedriver.zip -d /opt/ && \
    mv /opt/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf /tmp/chromedriver.zip /opt/chromedriver-linux64 && \
    chromedriver --version

# Копирование сертификатов
COPY src/main/resources/rt_ca_cer/*.cer /usr/local/share/ca-certificates/
RUN chmod 644 /usr/local/share/ca-certificates/*.cer && \
    update-ca-certificates && \
    echo "Certificates imported successfully"

WORKDIR /app

COPY build/libs/*.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "app.jar"]