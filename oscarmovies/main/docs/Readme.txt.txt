First Executed the Docker file. In VS Code - Right click and Build Image

Executed ComposeUp from VSCode

Once Image deployed in Docker container - then go to containers and go to VM - Exec

mysql -u root -p  - This will prompt the Password

mysql -u kishore -p

Execute MYSQL Comands
  - show databases;


  docker exec 453e3423bc7d470ae25faf24dad9a85f73935923d171c19f99ed282af951d36f mysql -u root -p pass /docker-entrypoint-initdb.d/data.sql

copy files to specific container:

docker cp Datafiles/. 52b42ac24c685ae56ef2fe0dad6ce823bc61349e0fad3a7e288a3c430def892b:/var/lib/mysql-files

docker cp application.properties a4e8cd39d352137afdfd46a13b59b782e6828b1c8fc76288e9ac61770bb17a4d:/config
To get the IP of the container ::
docker inspect \
-f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' container_name_or_id

mysqlimport --ignore-lines=1 \
--fields-terminated-by=, \
--local -u kishore \
-p pass \
/docker-entrypoint-initdb.d/movie_data.csv


LOAD DATA INFILE '/var/lib/mysql-files/movie_data.csv'
INTO TABLE movie
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;




LOAD DATA INFILE '/var/lib/mysql-files/genres_data.csv'
INTO TABLE genres
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA INFILE '/var/lib/mysql-files/movie_genres_data.csv'
INTO TABLE movie_genres
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


copy file in below location


mysql> SHOW VARIABLES LIKE "secure_file_priv";
+------------------+-----------------------+
| Variable_name | Value |
+------------------+-----------------------+
| secure_file_priv | /var/lib/mysql-files/ |
+------------------+-----------------------+
1 row in set (0.06 sec)


cp  /docker-entrypoint-initdb.d/*csv   /var/lib/mysql-files/