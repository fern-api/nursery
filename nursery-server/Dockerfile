FROM eclipse-temurin:11.0.13_8-jre-alpine

ARG BUILD_VERSION
COPY nursery-$BUILD_VERSION.sls.tgz /
RUN mkdir -p /opt \
    && tar -xvf /nursery-$BUILD_VERSION.sls.tgz -C /opt \
    && mv /opt/nursery-$BUILD_VERSION /opt/nursery \
    && rm -rf nursery-$BUILD_VERSION.sls.tgz

# Run server
ENTRYPOINT cd /opt/nursery \
    && ./service/bin/linux-amd64/go-java-launcher service/bin/launcher-static.yml var/conf/launcher-custom.yml