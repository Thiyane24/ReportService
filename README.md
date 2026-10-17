# ReportService (mock)

A tiny Spring Boot service that acts as a downstream report service behind GateX. It has one fast endpoint and one deliberately slow endpoint, so you can test GateX routing and its timeout handling.

**Stack:** Java 21, Spring Boot 4.1.1, Maven, Docker

## Endpoints

| Method | Path | Behavior |
|--------|------|----------|
| GET | `/api/reports/fast` | Responds immediately with a success message. |
| GET | `/api/reports/slow` | Sleeps 5 seconds, then responds. Use it to trigger a gateway timeout. |

The `/api` prefix is part of the route. GateX must not strip it for this service.

Example response for `GET /api/reports/fast`:

```json
{
  "status": "Success",
  "data": "This is a fast response."
}
```

## Run locally

With Maven (port 8082):

```
mvn spring-boot:run
```

With Docker:

```
docker build -t reportservice .
docker run -p 8082:8082 reportservice
```

Test it:

```
curl http://localhost:8082/api/reports/fast
curl http://localhost:8082/api/reports/slow
```

## Deploy to Heroku (Docker)

The port comes from Heroku's `PORT` variable (see `application.yaml`), and `heroku.yml` tells Heroku to build the `Dockerfile`.

```
heroku login
heroku create your-reportservice-mock --stack container
git init
git add .
git commit -m "ReportService mock"
git push heroku main
```

If your local branch is `master`, push with `git push heroku master:main`.

Check it:

```
heroku open /api/reports/fast
heroku logs --tail
```

## Connect GateX

Point the GateX route at the Heroku URL, and keep the `/api` prefix:

```
uri: https://your-reportservice-mock.herokuapp.com
predicates:
  - Path=/api/reports/**
```

Do not add `PreserveHostHeader` on this route. Heroku routes by the Host header, so a forwarded gateway host gives a 404.

## Testing the timeout

1. Set the GateX response timeout to 3 seconds for this route.
2. Call `/api/reports/fast` through GateX. It should return 200.
3. Call `/api/reports/slow` through GateX. It should return 504 after about 3 seconds.

## Notes

- Heroku's own router closes requests after 30 seconds. The 5 second sleep is well inside that, so only GateX's timeout will fire.
- Eco dynos sleep after 30 minutes without traffic, so the first request after a pause can take 10 to 30 seconds. Call `/fast` first to wake it up before running timeout tests, or the cold start will be mistaken for a slow response.
