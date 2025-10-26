# Dockerfile
FROM tomcat:9-jdk11

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WAR file
COPY BankingSystemWeb.war /usr/local/tomcat/webapps/ROOT.war

# Expose port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]