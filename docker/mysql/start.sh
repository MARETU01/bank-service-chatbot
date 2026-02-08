docker run -d \
  --name mysql \
  -p 3306:3306 \
  --restart on-failure \
  -e TZ=Asia/Shanghai \
  -e MYSQL_ROOT_PASSWORD='maretu' \
  -v ./data:/var/lib/mysql \
  -v ./conf:/etc/mysql/conf.d \
  -v ./init:/docker-entrypoint-initdb.d \
  --network maretu \
  mysql