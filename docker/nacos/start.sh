docker run --name nacos -d \
    -e TIME_ZONE='Asia/Shanghai' \
    -e MODE=standalone \
    -e SPRING_DATASOURCE_PLATFORM=mysql \
    -e MYSQL_SERVICE_HOST=mysql \
    -e MYSQL_SERVICE_PORT=3306 \
    -e MYSQL_SERVICE_DB_NAME=nacos \
    -e MYSQL_SERVICE_USER=root \
    -e MYSQL_SERVICE_PASSWORD=maretu \
    -e NACOS_AUTH_ENABLE=true \
    -e NACOS_AUTH_TOKEN=SFZzIXFHVEAz/FFifKQhbvvbxdCUJiwp/9FSNUDUn/2hy9bKTEyn6uqrP5C5YVg1KGGucvx58g2VDVL/WKj5zw== \
    -e NACOS_AUTH_IDENTITY_KEY=maretu \
    -e NACOS_AUTH_IDENTITY_VALUE=maretu \
    -p 8848:8848 \
    -p 9848:9848 \
     --network maretu \
    nacos/nacos-server:v2.5.2