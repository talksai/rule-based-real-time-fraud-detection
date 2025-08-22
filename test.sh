#!/bin/bash

echo "🚀 Starting End-to-End Test"

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 5

# Test 1: Single normal event
echo "📤 Sending normal event..."
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: $(uuidgen)" \
  -d '{
    "eventId": "test-001",
    "eventType": "login",
    "userId": "user-123",
    "timestamp": "2025-08-22T10:00:00Z",
    "ipAddress": "192.168.1.100",
    "userAgent": "test-browser/1.0",
    "amount": 100.0
  }' -w "\n"

sleep 2

# Test 2: Trigger velocity rule (multiple events)
echo "📤 Sending multiple events to trigger velocity rule..."
for i in {1..7}; do
  curl -X POST http://localhost:8080/api/v1/events \
    -H "Content-Type: application/json" \
    -H "X-Correlation-ID: $(uuidgen)" \
    -d '{
      "eventId": "velocity-'$i'",
      "eventType": "login",
      "userId": "user-velocity-test",
      "timestamp": "2025-08-22T10:0'$i':00Z",
      "ipAddress": "192.168.1.200",
      "userAgent": "test-browser/1.0",
      "amount": 50.0
    }' -w "\n"
  sleep 0.5
done

# Test 3: Trigger amount rule
echo "📤 Sending high amount event..."
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: $(uuidgen)" \
  -d '{
    "eventId": "high-amount-001",
    "eventType": "transaction",
    "userId": "user-big-spender",
    "timestamp": "2025-08-22T10:10:00Z",
    "ipAddress": "10.0.0.1",
    "userAgent": "test-browser/1.0",
    "amount": 1500.0
  }' -w "\n"

sleep 3

echo "✅ Test completed! Check the logs and Kafka UI at http://localhost:8090"
