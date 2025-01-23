# Stock Tracker Backend

Spring Boot backend for the Stock Tracker application.

## Environment Variables

Required environment variables for deployment:

```
SPRING_PROFILES_ACTIVE=prod
TWELVEDATA_API_KEY=your_api_key
ALPHAVANTAGE_API_KEY=your_api_key
FRONTEND_URL=your_frontend_url
```

## Deployment on Render

1. Create a new Web Service on Render
2. Connect your GitHub repository
3. Configure the service:
   - Name: `stock-tracker-backend`
   - Environment: `Docker`
   - Branch: `main`
   - Root Directory: `.` (root of the repository)
   - Environment Variables: Add all required environment variables
   - Health Check Path: `/actuator/health`

## Local Development

1. Clone the repository
2. Set up environment variables in `application.properties`
3. Run `mvn spring-boot:run`

The API will be available at `http://localhost:8080` 