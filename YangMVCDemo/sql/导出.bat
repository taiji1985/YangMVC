set dbname=teach
PATH=C:\dev\MySQL\bin;%PATH%
echo CREATE DATABASE `%dbname%` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;  >create.sql
echo use %dbname%; > back.sql
mysqldump -uroot -p123456 %dbname% >> back.sql
pause
