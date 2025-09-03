Пробный запрос:

curl --location 'http://localhost:8080' \
--header 'Content-Type: application/json' \
--data '{
    "systemPrompt": "Ты эксперт в области космонавтики. Ответь не длиннее 150 слов",
    "userPrompt": "Расскажи чем кормить космонавта"
}'
