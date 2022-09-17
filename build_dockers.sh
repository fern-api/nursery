version=$1

mkdir -p ./dockers

./gradlew :nursery-server:dockerTag"$version"
docker save nursery-server:"$version" -o dockers/nursery-server.tar

