docker run -d \
  --name redis-stack \
  -p 6379:6379 \
  -p 8001:8001 \
  -v ./data:/data \
  -e REDIS_ARGS="--requirepass maretu" \
  --network maretu \
  redis/redis-stack:latest