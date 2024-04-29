curl -X POST \
  http://localhost:8080/rest/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "address": "123 Main St"
}'
