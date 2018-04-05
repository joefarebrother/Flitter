#install heroku
sudo add-apt-repository "deb https://cli-assets.heroku.com/branches/stable/apt ./"
curl -L https://cli-assets.heroku.com/apt/release.key | sudo apt-key add -
sudo apt update
sudo apt install heroku 

#install sbt
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt update
sudo apt install sbt

#install java
sudo apt install openjdk-8-jdk-headless

#install postgres
sudo apt install postgresql

#setup postgres
me=$(whoami)
sudo -u postgres -s -- <<EOT
	createuser $me -s
	createdb $me
	echo "alter user $me with password 'pw'" | psql
EOT

echo "DATABASE_URL=postgres://$me:pw@localhost/$me" > .env

#start
sbt compile stage && heroku local