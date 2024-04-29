curl -X POST \
  http://localhost:8080/rest/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "mealIds": ["5268203c-de76-4921-a3e3-439db69c462a", "4237681a-441f-47fc-a747-8e0169bacea1"]
}'

