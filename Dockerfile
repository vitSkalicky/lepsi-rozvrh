# https://github.com/mingchen/docker-android-build-box
FROM mingc/android-build-box:1.20.0

RUN rm /etc/apt/sources.list.d/*

RUN apt-get update && \
    apt-get install -y jq
